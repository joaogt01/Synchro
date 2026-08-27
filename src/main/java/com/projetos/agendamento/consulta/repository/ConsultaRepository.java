package com.projetos.agendamento.consulta.repository;

import com.projetos.agendamento.consulta.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByProfissionalIdAndInicio(Long profissionalId, LocalDateTime inicio);

    boolean existsByPacienteIdAndInicio(Long pacienteId, LocalDateTime inicio);
}