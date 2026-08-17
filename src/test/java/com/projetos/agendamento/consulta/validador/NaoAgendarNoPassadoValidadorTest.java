package com.projetos.agendamento.consulta.validador;

import com.projetos.agendamento.consulta.entity.Consulta;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

public class NaoAgendarNoPassadoValidadorTest {

    private final NaoAgendarNoPassadoValidador validador = new NaoAgendarNoPassadoValidador();

    @Test
    void deve_lancar_excecao_para_horario_no_passado() {
        Consulta consulta = Consulta.builder()
                .inicio(LocalDateTime.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> validador.validar(consulta))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nao_deve_lancar_excecao_para_horario_futuro() {
        Consulta consulta = Consulta.builder()
                .inicio(LocalDateTime.now().plusDays(1))
                .build();

        assertThatCode(() -> validador.validar(consulta)).doesNotThrowAnyException();
    }
}