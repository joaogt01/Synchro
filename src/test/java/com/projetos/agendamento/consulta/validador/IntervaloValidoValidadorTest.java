package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntervaloValidoValidadorTest {

    private final IntervaloValidoValidador validador = new IntervaloValidoValidador();
    private static final LocalDate DATA_BASE = LocalDate.of(2026, 9, 14);

    @Test
    @DisplayName("Deve lançar exceção quando início for igual ao fim (duração zero)")
    void deve_lancar_excecao_quando_inicio_igual_ao_fim() {
        LocalDateTime momento = DATA_BASE.atTime(10, 0);
        Consulta consulta = Consulta.builder().inicio(momento).fim(momento).build();

        assertThatThrownBy(() -> validador.validar(consulta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("início deve ser anterior ao fim");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o fim for anterior ao início")
    void deve_lancar_excecao_quando_fim_e_anterior_ao_inicio() {
        LocalDateTime inicio = DATA_BASE.atTime(10, 0);
        LocalDateTime fim = DATA_BASE.atTime(9, 30);
        Consulta consulta = Consulta.builder().inicio(inicio).fim(fim).build();

        assertThatThrownBy(() -> validador.validar(consulta))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Não deve lançar exceção para intervalo válido (início estritamente anterior ao fim)")
    void nao_deve_lancar_excecao_para_intervalo_valido() {
        LocalDateTime inicio = DATA_BASE.atTime(10, 0);
        LocalDateTime fim = DATA_BASE.atTime(10, 45);
        Consulta consulta = Consulta.builder().inicio(inicio).fim(fim).build();

        assertThatCode(() -> validador.validar(consulta)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve lançar exceção quando início ou fim forem nulos")
    void deve_lancar_excecao_quando_datas_forem_nulas() {
        Consulta consultaSemInicio = Consulta.builder().fim(DATA_BASE.atTime(10, 0)).build();
        Consulta consultaSemFim = Consulta.builder().inicio(DATA_BASE.atTime(10, 0)).build();

        assertThatThrownBy(() -> validador.validar(consultaSemInicio))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> validador.validar(consultaSemFim))
                .isInstanceOf(IllegalArgumentException.class);
    }
}