package com.projetos.agendamento.autenticacao.service;

import com.projetos.agendamento.autenticacao.entity.RefreshToken;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.refresh.expiration-days:7}")
    private long expirationDays;

    public Duration ttl() {
        return Duration.ofDays(expirationDays);
    }

    /** Gera um novo refresh token e devolve o valor bruto (única vez em que ele existe em memória). */
    @Transactional
    public String emitir(Usuario usuario) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String bruto = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenRepository.save(RefreshToken.builder()
                .usuario(usuario)
                .tokenHash(hash(bruto))
                .expiraEm(LocalDateTime.now().plus(ttl()))
                .build());

        return bruto;
    }

    /**
     * Consome o token recebido e devolve o dono da sessão.
     * Um token já revogado indica reuso (cookie roubado): toda a família é derrubada.
     */
    @Transactional
    public Usuario consumir(String bruto) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(bruto))
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));

        LocalDateTime agora = LocalDateTime.now();

        if (token.getRevogadoEm() != null) {
            log.warn("Reuso de refresh token detectado para o usuário {}. Revogando todas as sessões.",
                    token.getUsuario().getId());
            refreshTokenRepository.revogarTodosDoUsuario(token.getUsuario().getId(), agora);
            throw new BadCredentialsException("Refresh token já utilizado");
        }

        if (!token.estaAtivo(agora)) {
            throw new BadCredentialsException("Refresh token expirado");
        }

        token.setRevogadoEm(agora);
        return token.getUsuario();
    }

    @Transactional
    public void revogar(String bruto) {
        refreshTokenRepository.findByTokenHash(hash(bruto))
                .filter(token -> token.getRevogadoEm() == null)
                .ifPresent(token -> token.setRevogadoEm(LocalDateTime.now()));
    }

    @Transactional
    public void revogarTodosDoUsuario(Long usuarioId) {
        refreshTokenRepository.revogarTodosDoUsuario(usuarioId, LocalDateTime.now());
    }

    private String hash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }
}
