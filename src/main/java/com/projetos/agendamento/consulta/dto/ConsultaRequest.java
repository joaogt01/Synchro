package com.projetos.agendamento.consulta.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConsultaRequest(
        @NotNull(message = "Profissional é obrigatório")
        Long profissionalId,

        @NotNull(message = "Paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "Horário de início é obrigatório")
        @Future(message = "O início da consulta deve ser no futuro")
        LocalDateTime inicio,

        @NotNull(message = "Horário de término é obrigatório")
        @Future(message = "O término da consulta deve ser no futuro")
        LocalDateTime fim
) {}