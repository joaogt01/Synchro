package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PacienteSemConflitoValidador implements ValidadorAgendamento{

    private final ConsultaRepository consultaRepository;

    @Override
    public void validar(Consulta consulta){
        boolean conflito = consultaRepository
                .existsByPacienteIdAndStartAt(consulta.getPaciente().getId(), consulta.getInicio());
        if (conflito){
            throw new IllegalArgumentException("Paciente já possui consulta agendada neste horário");
        }
    }
}