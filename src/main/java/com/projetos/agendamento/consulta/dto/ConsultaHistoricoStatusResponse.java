package com.projetos.agendamento.consulta.dto;

import com.projetos.agendamento.consulta.entity.StatusAgendamento;

import java.time.LocalDateTime;

public record ConsultaHistoricoStatusResponse(
        Long id,
        StatusAgendamento statusAnterior,
        StatusAgendamento statusNovo,
        String alteradoPorNome,
        LocalDateTime alteradoEm
) {}