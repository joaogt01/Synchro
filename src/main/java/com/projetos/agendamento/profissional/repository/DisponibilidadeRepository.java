package com.projetos.agendamento.profissional.repository;

import com.projetos.agendamento.profissional.entity.Disponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibilidadeRepository extends JpaRepository<Disponibilidade, Long> {
    List<Disponibilidade> findByProfissionalId(Long profissionalId);
}