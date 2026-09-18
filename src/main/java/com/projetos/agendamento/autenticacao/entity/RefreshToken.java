package com.projetos.agendamento.autenticacao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "revogado_em")
    private LocalDateTime revogadoEm;

    @PrePersist
    void naCriacao() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
    }

    public boolean estaAtivo(LocalDateTime agora) {
        return revogadoEm == null && expiraEm.isAfter(agora);
    }
}
