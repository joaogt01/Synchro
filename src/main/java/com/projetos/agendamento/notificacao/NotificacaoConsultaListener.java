package com.projetos.agendamento.notificacao;

import com.projetos.agendamento.consulta.evento.ConsultaAgendadaEvento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class NotificacaoConsultaListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAgendarConsulta(ConsultaAgendadaEvento evento) {
        enviarNotificacao(evento);
    }

    protected void enviarNotificacao(ConsultaAgendadaEvento event) {
        log.info("Notificação enviada: consulta {} agendada para {} com {} às {}",
                event.consultaId(), event.pacienteNome(), event.profissionalNome(), event.inicio());
    }
}