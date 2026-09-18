package com.projetos.agendamento.autenticacao.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class OrigemConfiavelFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Requested-By";
    public static final String VALOR = "synchro-web";

    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        boolean usaCookie = JwtAuthenticationFilter.lerCookie(request, CookieService.COOKIE_ACESSO).isPresent()
                || JwtAuthenticationFilter.lerCookie(request, CookieService.COOKIE_REFRESH).isPresent();

        if (usaCookie && !METODOS_SEGUROS.contains(request.getMethod())
                && !VALOR.equals(request.getHeader(HEADER))) {

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"status\":403,\"erro\":\"Acesso Negado\",\"mensagem\":\"Requisição sem o header " + HEADER + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
