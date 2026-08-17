package com.projetos.agendamento.profissional.repository;

import com.projetos.agendamento.profissional.entity.SlotBloqueado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SlotBloqueadoRepository extends JpaRepository<SlotBloqueado, Long> {
    List<SlotBloqueado> findByProfissionalIdAndStartAtBetween(Long profissionalId, LocalDateTime inicio, LocalDateTime fim);
}