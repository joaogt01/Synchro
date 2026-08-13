package com.projetos.agendamento.autenticacao.repository;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
