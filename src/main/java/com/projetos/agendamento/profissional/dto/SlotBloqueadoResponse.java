package com.projetos.agendamento.profissional.dto;

import java.time.LocalDateTime;

public record SlotBloqueadoResponse(
        Long id,
        Long profissionalId,
        LocalDateTime inicio,
        LocalDateTime fim
) {}