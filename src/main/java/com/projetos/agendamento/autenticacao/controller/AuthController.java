package com.projetos.agendamento.autenticacao.controller;

import com.projetos.agendamento.autenticacao.dto.LoginRequest;
import com.projetos.agendamento.autenticacao.dto.LoginResponse;
import com.projetos.agendamento.autenticacao.dto.RegistroRequest;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.security.JwtService;
import com.projetos.agendamento.autenticacao.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registrar(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = usuarioService.registrar(request);
        return LoginResponse.of(jwtService.gerarToken(usuario));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

            Usuario usuario = (Usuario) authentication.getPrincipal();
            return LoginResponse.of(jwtService.gerarToken(usuario));
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public void handleBadCredentials() {
    }
}