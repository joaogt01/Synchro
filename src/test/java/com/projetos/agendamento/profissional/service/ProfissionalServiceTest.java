package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.profissional.dto.ProfissionalRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalResponse;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void deve_criar_profissional_quando_usuario_existe() {
        Usuario usuario = Usuario.builder().id(1L).nome("Dra. Ana").build();
        Profissional saved = Profissional.builder().id(10L).usuario(usuario).especialidade("Dermatologia").ativo(true).build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(profissionalRepository.save(any(Profissional.class))).thenReturn(saved);

        ProfissionalResponse response = profissionalService.criar(new ProfissionalRequest(1L, "Dermatologia"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.especialidade()).isEqualTo("Dermatologia");
    }

    @Test
    void nao_deve_criar_profissional_quando_usuario_nao_existe() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profissionalService.criar(new ProfissionalRequest(99L, "Cardiologia")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deve_lancar_excecao_ao_buscar_profissional_inexistente() {
        when(profissionalRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profissionalService.buscarPorId(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void PROFISSIONAL_nao_deve_conseguir_atualizar_cadastro_de_outro_profissional() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Usuario atacante = Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(10L).usuario(dono).especialidade("Cardiologia").build();

        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));
        autenticarComo(atacante);

        assertThatThrownBy(() -> profissionalService.atualizarComOwnership(10L, new ProfissionalRequest(1L, "Dermatologia")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void PROFISSIONAL_deve_conseguir_atualizar_o_proprio_cadastro() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = Profissional.builder().id(10L).usuario(dono).especialidade("Cardiologia").build();

        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));
        autenticarComo(dono);

        ProfissionalResponse response = profissionalService.atualizarComOwnership(10L, new ProfissionalRequest(1L, "Dermatologia"));

        assertThat(response.especialidade()).isEqualTo("Dermatologia");
    }

    @Test
    void ADMIN_deve_conseguir_atualizar_qualquer_profissional() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Usuario admin = Usuario.builder().id(99L).role(UserRole.ADMIN).build();
        Profissional profissional = Profissional.builder().id(10L).usuario(dono).especialidade("Cardiologia").build();

        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));
        autenticarComo(admin);

        ProfissionalResponse response = profissionalService.atualizarComOwnership(10L, new ProfissionalRequest(1L, "Dermatologia"));

        assertThat(response.especialidade()).isEqualTo("Dermatologia");
    }
}