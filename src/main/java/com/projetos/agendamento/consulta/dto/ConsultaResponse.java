package com.projetos.agendamento.consulta.dto;

import com.projetos.agendamento.consulta.entity.StatusAgendamento;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        ProfissionalResumoResponse profissional,
        PacienteResumoResponse paciente,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusAgendamento status
) {
    public record ProfissionalResumoResponse(
            Long id,
            String nome
    ) {}

    public record PacienteResumoResponse(
            Long id,
            String nome
    ) {}
}