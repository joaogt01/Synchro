package com.projetos.agendamento.profissional.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfissionalAtualizarRequest(
        @NotBlank(message = "Especialidade é obrigatória")
        String especialidade
) {}