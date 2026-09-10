package com.projetos.agendamento.consulta.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConsultaRequest(
        @NotNull(message = "O ID do profissional é obrigatório")
        Long profissionalId,

        @NotNull(message = "O ID do paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "A data/hora de início é obrigatória")
        @Future(message = "A data de início deve ser no futuro")
        LocalDateTime inicio,

        @NotNull(message = "A data/hora de término é obrigatória")
        @Future(message = "A data de término deve ser no futuro")
        LocalDateTime fim
) {}