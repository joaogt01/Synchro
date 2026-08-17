package com.projetos.agendamento.consulta.service;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.validador.ValidadorAgendamento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrquestradorValidacaoAgendamento {

    private final List<ValidadorAgendamento> validadores;

    public void validarTodos(Consulta consulta) {
        validadores.forEach(validador -> validador.validar(consulta));
    }
}