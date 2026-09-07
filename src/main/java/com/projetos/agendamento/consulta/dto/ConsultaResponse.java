package com.projetos.agendamento.consulta.dto;

import com.projetos.agendamento.consulta.entity.StatusAgendamento;
<<<<<<< HEAD

=======
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)
import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
<<<<<<< HEAD
        Long profissionalId,
        String profissionalNome,
        Long pacienteId,
        String pacienteNome,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusAgendamento status,
        LocalDateTime criadoEm
) {}
=======
        ProfissionalResumoResponse profissional,
        PacienteResumoResponse paciente,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusAgendamento status
) {
    public record ProfissionalResumoResponse(
            Long id,
            String nome
    ) {}

    public record PacienteResumoResponse(
            Long id,
            String nome
    ) {}
}
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)
