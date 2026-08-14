package com.projetos.agendamento.paciente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PacienteRequest (
        @NotNull(message = "IdUsuario é obrigatório")
        Long usuarioId,

        @NotBlank(message = "telefone é obrigatorio")
        String telefone
) {}