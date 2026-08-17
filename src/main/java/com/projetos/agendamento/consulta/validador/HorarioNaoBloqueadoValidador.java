package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.profissional.repository.SlotBloqueadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HorarioNaoBloqueadoValidador implements ValidadorAgendamento{

    private final SlotBloqueadoRepository slotBloqueadoRepository;

    @Override
    public void validar(Consulta consulta) {
        boolean bloqueado = !slotBloqueadoRepository
                .findByProfissionalIdAndStartAtBetween(
                        consulta.getProfissional().getId(),
                        consulta.getInicio(),
                        consulta.getFim()
                ).isEmpty();

        if (bloqueado){
            throw new IllegalArgumentException("Horário está bloqueado pelo profissional");
        }
    }
}