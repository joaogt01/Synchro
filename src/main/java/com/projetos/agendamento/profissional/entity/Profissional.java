package com.projetos.agendamento.profissional.entity;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profissionais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String especialidade;

    @Column(nullable = false)
    private boolean ativo;
}
