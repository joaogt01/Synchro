package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HorarioDisponivelValidador implements ValidadorAgendamento{

    private final ConsultaRepository consultaRepository;

    @Override
    public void validar(Consulta consulta){
        boolean ocupado = consultaRepository
                .existsByProfissionalIdAndStartsAt(consulta.getProfissional().getId(), consulta.getInicio());

        if (ocupado){
            throw new IllegalArgumentException("Horário já está ocupado para este profissional");
        }
    }
}