package com.projetos.agendamento.consulta.entity;

import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.profissional.entity.Profissional;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "consulta",
        uniqueConstraints = @UniqueConstraint(
        name = "uq_profissional_slot",
        columnNames = {"profissional_id", "inicio"}
    )
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fim", nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void naCriacao() {
        criadoEm = LocalDateTime.now();
        if (status == null) status = StatusAgendamento.AGENDADO;
    }
}
