package com.projetos.agendamento.profissional.dto;

import com.projetos.agendamento.profissional.entity.SlotBloqueado;

public class SlotBloqueadoMapper {

    private SlotBloqueadoMapper() {}

    public static SlotBloqueadoResponse toResponse(SlotBloqueado slotBloqueado) {
        return new SlotBloqueadoResponse(
                slotBloqueado.getId(),
                slotBloqueado.getProfissional().getId(),
                slotBloqueado.getInicio(),
                slotBloqueado.getFim()
        );
    }
}