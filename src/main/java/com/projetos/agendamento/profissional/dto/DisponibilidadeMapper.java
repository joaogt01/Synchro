package com.projetos.agendamento.profissional.dto;

import com.projetos.agendamento.profissional.entity.Disponibilidade;

public class DisponibilidadeMapper {

    private DisponibilidadeMapper() {}

    public static DisponibilidadeResponse toResponse(Disponibilidade disponibilidade){
        return new DisponibilidadeResponse(
                disponibilidade.getId(),
                disponibilidade.getProfissional().getId(),
                disponibilidade.getDiaDaSemana(),
                disponibilidade.getInicio(),
                disponibilidade.getFim()
        );
    }
}
