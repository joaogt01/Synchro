package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HorarioDisponivelValidador implements ValidadorAgendamento {

    private final ConsultaRepository consultaRepository;

    @Override
    public void validar(Consulta consulta) {
        boolean ocupado = consultaRepository
                .buscarConflitosDeHorarioProfissional(
                        consulta.getProfissional().getId(), consulta.getInicio(), consulta.getFim())
                .stream()
                .anyMatch(existente -> !existente.getId().equals(consulta.getId()));

        if (ocupado) {
            throw new IllegalArgumentException("Horário já está ocupado para este profissional");
        }
    }
}