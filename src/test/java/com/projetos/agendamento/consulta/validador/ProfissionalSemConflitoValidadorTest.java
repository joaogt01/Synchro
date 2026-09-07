package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.entity.StatusAgendamento;
import com.projetos.agendamento.consulta.repository.ConsultaRepository;
import com.projetos.agendamento.profissional.entity.Profissional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfissionalSemConflitoValidadorTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private ProfissionalSemConflitoValidador validador;

    private static final LocalDate DATA_BASE = LocalDate.of(2026, 9, 14);
    private final LocalDateTime baseInicio = DATA_BASE.atTime(10, 0);
    private final LocalDateTime baseFim = DATA_BASE.atTime(11, 0);

    private Consulta consultaExistente(Long id) {
        return Consulta.builder()
                .id(id)
                .profissional(Profissional.builder().id(10L).build())
                .inicio(baseInicio)
                .fim(baseFim)
                .status(StatusAgendamento.AGENDADO)
                .build();
    }

    private Consulta novaConsulta(LocalDateTime inicio, LocalDateTime fim) {
        return Consulta.builder()
                .profissional(Profissional.builder().id(10L).build())
                .inicio(inicio)
                .fim(fim)
                .status(StatusAgendamento.AGENDADO)
                .build();
    }

    @ParameterizedTest(name = "sobreposição: nova consulta {0}-{1} colide com existente 10:00-11:00")
    @CsvSource({
            "09:30, 10:30",
            "10:15, 10:45",
            "10:30, 11:30",
            "09:00, 12:00"
    })
    @DisplayName("Deve lançar exceção quando houver sobreposição parcial ou total com consulta existente")
    void deve_detectar_sobreposicao_parcial_ou_total(String horaInicio, String horaFim) {
        LocalDateTime inicio = DATA_BASE.atTime(LocalTime.parse(horaInicio));
        LocalDateTime fim = DATA_BASE.atTime(LocalTime.parse(horaFim));

        Consulta nova = novaConsulta(inicio, fim);

        when(consultaRepository.buscarConflitosDeHorarioProfissional(eq(10L), any(), any()))
                .thenReturn(List.of(consultaExistente(1L)));

        assertThatThrownBy(() -> validador.validar(nova))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("intervalo de horário");
    }

    @Test
    @DisplayName("Não deve lançar exceção quando o horário estiver totalmente livre")
    void nao_deve_lancar_excecao_quando_nao_ha_sobreposicao() {
        Consulta nova = novaConsulta(
                DATA_BASE.atTime(11, 0),
                DATA_BASE.atTime(12, 0));

        when(consultaRepository.buscarConflitosDeHorarioProfissional(eq(10L), any(), any()))
                .thenReturn(List.of());

        assertThatCode(() -> validador.validar(nova)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve considerar a própria consulta ao reagendar ou revalidar uma consulta existente")
    void nao_deve_considerar_a_propria_consulta_como_conflito_ao_reavaliar() {
        Consulta existente = consultaExistente(1L);

        when(consultaRepository.buscarConflitosDeHorarioProfissional(eq(10L), any(), any()))
                .thenReturn(List.of(existente));

        assertThatCode(() -> validador.validar(existente)).doesNotThrowAnyException();
    }
}