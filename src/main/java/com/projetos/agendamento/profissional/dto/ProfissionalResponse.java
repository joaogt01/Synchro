package com.projetos.agendamento.profissional.dto;

public record ProfissionalResponse(
    Long id,
    Long idUsuario,
    String nome,
    String especialidade,
    boolean ativo
) {}