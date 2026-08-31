package com.projetos.agendamento.consulta.repository;

import com.projetos.agendamento.consulta.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByProfissionalIdAndInicio(Long profissionalId, LocalDateTime inicio);

    boolean existsByPacienteIdAndInicio(Long pacienteId, LocalDateTime inicio);

    @Query("""
            SELECT c FROM Consulta c
            WHERE (:profissionalId IS NULL OR c.profissional.id = :profissionalId)
              AND (:pacienteId IS NULL OR c.paciente.id = :pacienteId)
              AND (:inicio IS NULL OR c.inicio >= :inicio)
              AND (:fim IS NULL OR c.inicio <= :fim)
            ORDER BY c.inicio
            """)
    List<Consulta> buscarComFiltros(Long profissionalId, Long pacienteId, LocalDateTime inicio, LocalDateTime fim);
}