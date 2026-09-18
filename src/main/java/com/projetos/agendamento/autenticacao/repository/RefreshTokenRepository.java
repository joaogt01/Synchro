package com.projetos.agendamento.autenticacao.repository;

import com.projetos.agendamento.autenticacao.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
               SET rt.revogadoEm = :agora
             WHERE rt.usuario.id = :usuarioId
               AND rt.revogadoEm IS NULL
            """)
    int revogarTodosDoUsuario(Long usuarioId, LocalDateTime agora);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiraEm < :limite")
    int apagarExpirados(LocalDateTime limite);
}
