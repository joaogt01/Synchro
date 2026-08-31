package com.projetos.agendamento.consulta.dto;

import com.projetos.agendamento.consulta.entity.Consulta;

public class ConsultaMapper {

    private ConsultaMapper() {}

    public static ConsultaResponse toResponse(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getProfissional().getId(),
                consulta.getProfissional().getUsuario().getNome(),
                consulta.getPaciente().getId(),
                consulta.getPaciente().getUsuario().getNome(),
                consulta.getInicio(),
                consulta.getFim(),
                consulta.getStatus(),
                consulta.getCriadoEm()
        );
    }
}