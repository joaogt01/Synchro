package com.projetos.agendamento.consulta.dto;

import com.projetos.agendamento.consulta.entity.Consulta;

public class ConsultaMapper {

<<<<<<< HEAD
    private ConsultaMapper() {}

    public static ConsultaResponse toResponse(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getProfissional().getId(),
                consulta.getProfissional().getUsuario().getNome(),
                consulta.getPaciente().getId(),
                consulta.getPaciente().getUsuario().getNome(),
                consulta.getInicio(),
                consulta.getFim(),
                consulta.getStatus(),
                consulta.getCriadoEm()
=======
    private ConsultaMapper() {
    }

    public static ConsultaResponse toResponse(Consulta consulta) {
        if (consulta == null) {
            return null;
        }

        ConsultaResponse.ProfissionalResumoResponse profissionalResumo = null;
        if (consulta.getProfissional() != null) {
            String nomeProfissional = consulta.getProfissional().getUsuario() != null
                    ? consulta.getProfissional().getUsuario().getNome()
                    : null;

            profissionalResumo = new ConsultaResponse.ProfissionalResumoResponse(
                    consulta.getProfissional().getId(),
                    nomeProfissional
            );
        }

        ConsultaResponse.PacienteResumoResponse pacienteResumo = null;
        if (consulta.getPaciente() != null) {
            String nomePaciente = consulta.getPaciente().getUsuario() != null
                    ? consulta.getPaciente().getUsuario().getNome()
                    : null;

            pacienteResumo = new ConsultaResponse.PacienteResumoResponse(
                    consulta.getPaciente().getId(),
                    nomePaciente
            );
        }

        return new ConsultaResponse(
                consulta.getId(),
                profissionalResumo,
                pacienteResumo,
                consulta.getInicio(),
                consulta.getFim(),
                consulta.getStatus()
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)
        );
    }
}