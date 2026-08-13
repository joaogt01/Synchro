package com.projetos.agendamento.utils.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError (
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
        ) {}