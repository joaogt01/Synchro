package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.profissional.entity.Disponibilidade;
import com.projetos.agendamento.profissional.repository.DisponibilidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DentroDoHorarioAtendimentoValidador implements ValidadorAgendamento {

    private final DisponibilidadeRepository disponibilidadeRepository;

    @Override
    public void validar(Consulta consulta){
        Long profissionalId = consulta.getProfissional().getId();
        LocalTime horario = consulta.getInicio().toLocalTime();
        short diaDaSemana = (short) consulta.getInicio().getDayOfWeek().getValue();

        List<Disponibilidade> disponibilidades = disponibilidadeRepository.findByProfissionalId(profissionalId);

        boolean dentroDoHorario = disponibilidades.stream()
                .filter(disponibilidade -> disponibilidade.getDiaDaSemana() == diaDaSemana)
                .anyMatch(disponibilidade -> !horario.isBefore(disponibilidade.getInicio()) && !horario.isAfter(disponibilidade.getFim()));
        if (!dentroDoHorario){
            throw new IllegalArgumentException("Horário fora da disponibilidade do profissional");
        }
    }
}