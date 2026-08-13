package com.projetos.agendamento.profissional.repository;

import com.projetos.agendamento.profissional.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
    Optional<Profissional> buscarUsuarioPorId(Long idUsuario);
}
