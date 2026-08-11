package com.projetos.agendamento.profissional.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "disponibilidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Disponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(name = "dia_da_semana", nullable = false)
    private Short diaDaSemana;

    @Column(name = "inicio", nullable = false)
    private LocalTime inicio;

    @Column(name = "fim", nullable = false)
    private LocalTime fim;
}
