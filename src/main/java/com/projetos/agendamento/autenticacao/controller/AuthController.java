package com.projetos.agendamento.autenticacao.controller;

import com.projetos.agendamento.autenticacao.dto.LoginRequest;
import com.projetos.agendamento.autenticacao.dto.RegistroRequest;
import com.projetos.agendamento.autenticacao.dto.UsuarioMapper;
import com.projetos.agendamento.autenticacao.dto.UsuarioResponse;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.security.CookieService;
import com.projetos.agendamento.autenticacao.security.JwtAuthenticationFilter;
import com.projetos.agendamento.autenticacao.security.JwtService;
import com.projetos.agendamento.autenticacao.service.RefreshTokenService;
import com.projetos.agendamento.autenticacao.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request, HttpServletResponse response) {
        Usuario usuario = usuarioService.registrar(request);
        abrirSessao(usuario, response);
        return UsuarioMapper.toResponse(usuario);
    }

    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

            Usuario usuario = (Usuario) authentication.getPrincipal();
            abrirSessao(usuario, response);
            return UsuarioMapper.toResponse(usuario);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }
    }

    @PostMapping("/refresh")
    public UsuarioResponse renovar(HttpServletRequest request, HttpServletResponse response) {
        String refresh = lerRefresh(request)
                .orElseThrow(() -> new BadCredentialsException("Refresh token ausente"));

        Usuario usuario = refreshTokenService.consumir(refresh);
        abrirSessao(usuario, response);
        return UsuarioMapper.toResponse(usuario);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        lerRefresh(request).ifPresent(refreshTokenService::revogar);
        adicionar(response, cookieService.limparAcesso());
        adicionar(response, cookieService.limparRefresh());
    }

    private void abrirSessao(Usuario usuario, HttpServletResponse response) {
        String acesso = jwtService.gerarToken(usuario);
        String refresh = refreshTokenService.emitir(usuario);

        adicionar(response, cookieService.acesso(acesso, jwtService.ttlAcesso()));
        adicionar(response, cookieService.refresh(refresh, refreshTokenService.ttl()));
    }

    private Optional<String> lerRefresh(HttpServletRequest request) {
        return JwtAuthenticationFilter.lerCookie(request, CookieService.COOKIE_REFRESH);
    }

    private void adicionar(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public void handleBadCredentials() {
    }
}
