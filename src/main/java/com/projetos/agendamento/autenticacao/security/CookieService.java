package com.projetos.agendamento.autenticacao.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    public static final String COOKIE_ACESSO = "synchro_access";
    public static final String COOKIE_REFRESH = "synchro_refresh";
    private static final String CAMINHO_REFRESH = "/api/auth";

    private final boolean secure;
    private final String sameSite;
    private final String dominio;

    public CookieService(
            @Value("${app.cookie.secure:true}") boolean secure,
            @Value("${app.cookie.same-site:Lax}") String sameSite,
            @Value("${app.cookie.domain:}") String dominio) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.dominio = dominio;
    }

    public ResponseCookie acesso(String token, Duration ttl) {
        return base(COOKIE_ACESSO, token, "/", ttl).build();
    }

    public ResponseCookie refresh(String token, Duration ttl) {
        return base(COOKIE_REFRESH, token, CAMINHO_REFRESH, ttl).build();
    }

    public ResponseCookie limparAcesso() {
        return base(COOKIE_ACESSO, "", "/", Duration.ZERO).build();
    }

    public ResponseCookie limparRefresh() {
        return base(COOKIE_REFRESH, "", CAMINHO_REFRESH, Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String nome, String valor, String caminho, Duration ttl) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(caminho)
                .maxAge(ttl);

        if (dominio != null && !dominio.isBlank()) {
            builder.domain(dominio);
        }
        return builder;
    }
}
