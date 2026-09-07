package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class IntervaloValidoValidador implements ValidadorAgendamento {

    @Override
    public void validar(Consulta consulta) {
        if (consulta.getInicio() == null || consulta.getFim() == null) {
            throw new IllegalArgumentException("Início e fim da consulta são obrigatórios");
        }
        if (!consulta.getInicio().isBefore(consulta.getFim())) {
            throw new IllegalArgumentException("O horário de início deve ser anterior ao horário de término");
        }
    }
}