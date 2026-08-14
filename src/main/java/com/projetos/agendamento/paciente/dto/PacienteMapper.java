package com.projetos.agendamento.paciente.dto;

import com.projetos.agendamento.paciente.entity.Paciente;

public class PacienteMapper {

    private PacienteMapper(){}

    public static PacienteResponse toResponse(Paciente paciente){
        return new PacienteResponse(
                paciente.getId(),
                paciente.getUsuario().getId(),
                paciente.getUsuario().getNome(),
                paciente.getTelefone()
        );
    }
}