package com.projetos.agendamento.consulta.evento;

import java.time.LocalDateTime;

public record ConsultaAgendadaEvento(
        Long consultaId,
        Long profissionalId,
        String profissionalNome,
        Long pacienteId,
        String pacienteNome,
        LocalDateTime inicio,
        LocalDateTime fim
){}