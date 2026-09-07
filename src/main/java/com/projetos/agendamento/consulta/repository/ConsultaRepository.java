package com.projetos.agendamento.consulta.repository;

import com.projetos.agendamento.consulta.entity.Consulta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c FROM Consulta c
            WHERE c.profissional.id = :profissionalId
              AND c.status <> com.projetos.agendamento.consulta.entity.StatusAgendamento.CANCELADO
              AND c.inicio < :fim AND c.fim > :inicio
            """)
    List<Consulta> buscarConflitosDeHorarioProfissional(Long profissionalId, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            SELECT c FROM Consulta c
            WHERE c.paciente.id = :pacienteId
              AND c.status <> com.projetos.agendamento.consulta.entity.StatusAgendamento.CANCELADO
              AND c.inicio < :fim AND c.fim > :inicio
            """)
    List<Consulta> buscarConflitosDeHorarioPaciente(Long pacienteId, LocalDateTime inicio, LocalDateTime fim);

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