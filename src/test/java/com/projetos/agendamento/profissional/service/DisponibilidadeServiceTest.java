package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.profissional.dto.DisponibilidadeRequest;
import com.projetos.agendamento.profissional.entity.Disponibilidade;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.DisponibilidadeRepository;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DisponibilidadeServiceTest {
    @Mock
    private DisponibilidadeRepository disponibilidadeRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private DisponibilidadeService disponibilidadeService;

    @Test
    void nao_deve_cadastrar_disponibilidade_sobreposta() {
        Profissional profissional = Profissional.builder().id(1L).build();
        Disponibilidade existente = Disponibilidade.builder()
                .profissional(profissional)
                .diaDaSemana((short) 1)
                .inicio(LocalTime.of(8, 0))
                .fim(LocalTime.of(12, 0))
                .build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(disponibilidadeRepository.findByProfissionalId(1L)).thenReturn(List.of(existente));

        DisponibilidadeRequest request = new DisponibilidadeRequest((short) 1, LocalTime.of(10, 0), LocalTime.of(14, 0));

        assertThatThrownBy(() -> disponibilidadeService.cadastrar(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
