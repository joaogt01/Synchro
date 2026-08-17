package com.projetos.agendamento.profissional.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record DisponibilidadeRequest(
        @NotNull(message = "diaDaSemana é obrigatorio")
        Short diaDaSemana,

        @NotNull(message = "horaInicio é obrigatório")
        LocalTime horaInicio,

        @NotNull(message = "horaFim é obrigatório")
        LocalTime horaFim
) {}