package com.projetos.agendamento.notificacao;

import com.projetos.agendamento.consulta.evento.ConsultaAgendadaEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacaoConsultaListenerTest {

    @Spy
    private NotificacaoConsultaListener listener;

    private static final LocalDate DATA_FIXA = LocalDate.of(2026, 9, 14);

    @Test
    @DisplayName("Deve executar o envio de notificação real mantendo o contrato original")
    void deve_processar_notificacao_ao_receber_o_evento() {
        LocalDateTime inicio = DATA_FIXA.atTime(10, 0);
        LocalDateTime fim = inicio.plusMinutes(30);

        ConsultaAgendadaEvento evento = new ConsultaAgendadaEvento(
                1L, 10L, "Dra. Ana", 20L, "João", inicio, fim);

        listener.aoAgendarConsulta(evento);

        verify(listener).enviarNotificacao(evento);
    }
}