package com.projetos.agendamento.profissional.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SlotBloqueadoRequest(
        @NotNull(message = "Inicio é obrigatorio")
        LocalDateTime inicio,

        @NotNull(message = "Fim é obrigatorio")
        LocalDateTime fim
) {}