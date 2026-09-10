package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfissionalSemConflitoValidador implements ValidadorAgendamento {

    private final ConsultaRepository consultaRepository;

    @Override
    public void validar(Consulta consulta) {
        boolean conflito = consultaRepository
                .buscarConflitosDeHorarioProfissional(
                        consulta.getProfissional().getId(), consulta.getInicio(), consulta.getFim())
                .stream()
                .anyMatch(existente -> !existente.getId().equals(consulta.getId()));

        if (conflito) {
            throw new IllegalArgumentException("Profissional já possui consulta agendada neste intervalo de horário");
        }
    }
}