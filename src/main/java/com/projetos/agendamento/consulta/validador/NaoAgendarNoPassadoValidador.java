package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NaoAgendarNoPassadoValidador implements ValidadorAgendamento{
    @Override
    public void validar(Consulta consulta){
        if (consulta.getInicio().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Não é possivel agendar uma consulta no passado");
        }
    }
}
