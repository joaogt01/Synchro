package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.profissional.dto.DisponibilidadeRequest;
import com.projetos.agendamento.profissional.entity.Disponibilidade;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.DisponibilidadeRepository;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void nao_deve_cadastrar_disponibilidade_sobreposta() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();
        Disponibilidade existente = Disponibilidade.builder()
                .profissional(profissional)
                .diaDaSemana((short) 1)
                .inicio(LocalTime.of(8, 0))
                .fim(LocalTime.of(12, 0))
                .build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(disponibilidadeRepository.findByProfissionalId(1L)).thenReturn(List.of(existente));
        autenticarComo(dono);

        DisponibilidadeRequest request = new DisponibilidadeRequest((short) 1, LocalTime.of(10, 0), LocalTime.of(14, 0));

        assertThatThrownBy(() -> disponibilidadeService.cadastrarComOwnership(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void PROFISSIONAL_nao_deve_conseguir_cadastrar_disponibilidade_em_nome_de_outro() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Usuario atacante = Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        autenticarComo(atacante);

        DisponibilidadeRequest request = new DisponibilidadeRequest((short) 2, LocalTime.of(9, 0), LocalTime.of(11, 0));

        assertThatThrownBy(() -> disponibilidadeService.cadastrarComOwnership(1L, request))
                .isInstanceOf(AccessDeniedException.class);
    }
}