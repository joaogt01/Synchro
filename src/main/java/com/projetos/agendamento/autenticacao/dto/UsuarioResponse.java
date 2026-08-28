package com.projetos.agendamento.autenticacao.dto;

import com.projetos.agendamento.autenticacao.entity.UserRole;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        UserRole role
) {}