package com.projetos.agendamento.consulta.dto;

import com.projetos.agendamento.consulta.entity.StatusAgendamento;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        Long profissionalId,
        String profissionalNome,
        Long pacienteId,
        String pacienteNome,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusAgendamento status,
        LocalDateTime criadoEm
) {}