package com.projetos.agendamento.profissional.dto;

import java.time.LocalTime;

public record DisponibilidadeResponse(
        Long id,
        Long profissionalId,
        Short diaDaSemana,
        LocalTime horaInicio,
        LocalTime horaFim
) {}