package com.projetos.agendamento.profissional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfissionalRequest(
        @NotNull (message = "Id obrigatório")
        Long idUsuario,

        @NotBlank(message = "Especialidade é obrigatória")
        String especialidade ) {}
