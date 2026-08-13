package com.projetos.agendamento.profissional.dto;

import com.projetos.agendamento.profissional.entity.Profissional;

public class ProfissionalMapper {

    private ProfissionalMapper() {}

    public static ProfissionalResponse toResponse(Profissional profissional){
        return new ProfissionalResponse(
                profissional.getId(),
                profissional.getUsuario().getId(),
                profissional.getUsuario().getNome(),
                profissional.getEspecialidade(),
                profissional.isAtivo()
        );
    }

}
