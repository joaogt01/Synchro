package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.profissional.dto.SlotBloqueadoRequest;
import com.projetos.agendamento.profissional.dto.SlotBloqueadoResponse;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.entity.SlotBloqueado;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.profissional.repository.SlotBloqueadoRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotBloqueadoServiceTest {

    @Mock
    private SlotBloqueadoRepository slotBloqueadoRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @InjectMocks
    private SlotBloqueadoService slotBloqueadoService;

    private static final LocalDate DATA_FUTURA = LocalDate.of(2026, 12, 14);

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void PROFISSIONAL_deve_conseguir_bloquear_o_proprio_horario() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(slotBloqueadoRepository.findByProfissionalId(1L)).thenReturn(List.of());
        when(slotBloqueadoRepository.save(any(SlotBloqueado.class))).thenAnswer(invocation -> {
            SlotBloqueado s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        autenticarComo(dono);

        SlotBloqueadoRequest request = new SlotBloqueadoRequest(DATA_FUTURA.atTime(14, 0), DATA_FUTURA.atTime(15, 0));

        SlotBloqueadoResponse response = slotBloqueadoService.cadastrarComOwnership(1L, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.profissionalId()).isEqualTo(1L);
    }

    @Test
    void PROFISSIONAL_nao_deve_conseguir_bloquear_horario_em_nome_de_outro() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Usuario atacante = Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        autenticarComo(atacante);

        SlotBloqueadoRequest request = new SlotBloqueadoRequest(DATA_FUTURA.atTime(14, 0), DATA_FUTURA.atTime(15, 0));

        assertThatThrownBy(() -> slotBloqueadoService.cadastrarComOwnership(1L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ADMIN_deve_conseguir_bloquear_horario_de_qualquer_profissional() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Usuario admin = Usuario.builder().id(99L).role(UserRole.ADMIN).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(slotBloqueadoRepository.findByProfissionalId(1L)).thenReturn(List.of());
        when(slotBloqueadoRepository.save(any(SlotBloqueado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        autenticarComo(admin);

        SlotBloqueadoRequest request = new SlotBloqueadoRequest(DATA_FUTURA.atTime(14, 0), DATA_FUTURA.atTime(15, 0));

        SlotBloqueadoResponse response = slotBloqueadoService.cadastrarComOwnership(1L, request);

        assertThat(response.profissionalId()).isEqualTo(1L);
    }

    @Test
    void nao_deve_bloquear_quando_fim_nao_e_posterior_ao_inicio() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        autenticarComo(dono);

        LocalDateTime momento = DATA_FUTURA.atTime(14, 0);
        SlotBloqueadoRequest request = new SlotBloqueadoRequest(momento, momento);

        assertThatThrownBy(() -> slotBloqueadoService.cadastrarComOwnership(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("anterior ao horário de término");
    }

    @Test
    void nao_deve_bloquear_horario_no_passado() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        autenticarComo(dono);

        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        SlotBloqueadoRequest request = new SlotBloqueadoRequest(inicio, inicio.plusHours(1));

        assertThatThrownBy(() -> slotBloqueadoService.cadastrarComOwnership(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("passado");
    }

    @Test
    void nao_deve_cadastrar_bloqueio_sobreposto() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();
        SlotBloqueado existente = SlotBloqueado.builder()
                .profissional(profissional)
                .inicio(DATA_FUTURA.atTime(14, 0))
                .fim(DATA_FUTURA.atTime(16, 0))
                .build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(slotBloqueadoRepository.findByProfissionalId(1L)).thenReturn(List.of(existente));
        autenticarComo(dono);

        SlotBloqueadoRequest request = new SlotBloqueadoRequest(DATA_FUTURA.atTime(15, 0), DATA_FUTURA.atTime(17, 0));

        assertThatThrownBy(() -> slotBloqueadoService.cadastrarComOwnership(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sobreposto");
    }

    @Test
    void deve_lancar_not_found_ao_bloquear_para_profissional_inexistente() {
        when(profissionalRepository.findById(99L)).thenReturn(Optional.empty());

        SlotBloqueadoRequest request = new SlotBloqueadoRequest(DATA_FUTURA.atTime(14, 0), DATA_FUTURA.atTime(15, 0));

        assertThatThrownBy(() -> slotBloqueadoService.cadastrarComOwnership(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void PROFISSIONAL_deve_conseguir_deletar_o_proprio_bloqueio() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();
        SlotBloqueado slot = SlotBloqueado.builder().id(5L).profissional(profissional).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(slotBloqueadoRepository.findById(5L)).thenReturn(Optional.of(slot));
        autenticarComo(dono);

        slotBloqueadoService.deletarComOwnership(1L, 5L);

        verify(slotBloqueadoRepository).delete(slot);
    }

    @Test
    void PROFISSIONAL_nao_deve_conseguir_deletar_bloqueio_de_outro() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Usuario atacante = Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(1L).usuario(dono).build();

        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        autenticarComo(atacante);

        assertThatThrownBy(() -> slotBloqueadoService.deletarComOwnership(1L, 5L))
                .isInstanceOf(AccessDeniedException.class);

        verify(slotBloqueadoRepository, never()).delete(any());
    }
}