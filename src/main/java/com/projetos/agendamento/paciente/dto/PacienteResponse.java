package com.projetos.agendamento.paciente.dto;

public record PacienteResponse(
        Long id,
        Long usuarioId,
        String nomeUsuario,
        String telefone
) {}