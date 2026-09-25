package com.projetos.agendamento.consulta.repository;

import com.projetos.agendamento.consulta.entity.ConsultaHistoricoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaHistoricoStatusRepository extends JpaRepository<ConsultaHistoricoStatus, Long> {
    List<ConsultaHistoricoStatus> findByConsultaIdOrderByAlteradoEmAsc(Long consultaId);
}