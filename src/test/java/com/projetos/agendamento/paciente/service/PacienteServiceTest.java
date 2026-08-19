package com.projetos.agendamento.paciente.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.paciente.dto.PacienteRequest;
import com.projetos.agendamento.paciente.dto.PacienteResponse;
import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.paciente.repository.PacienteRepository;
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
public class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void deve_criar_paciente_quando_usuario_existe() {
        Usuario usuario = Usuario.builder().id(1L).nome("João").build();
        Paciente salvo = Paciente.builder().id(10L).usuario(usuario).telefone("81999999999").build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(salvo);

        PacienteResponse response = pacienteService.criar(new PacienteRequest(1L, "81999999999"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.telefone()).isEqualTo("81999999999");
    }

    @Test
    void nao_deve_criar_paciente_quando_usuario_nao_existe() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.criar(new PacienteRequest(99L, "81988888888")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deve_lancar_excecao_ao_buscar_paciente_inexistente() {
        when(pacienteRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pacienteService.buscarPorIdComOwnership(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void PACIENTE_deve_conseguir_buscar_o_proprio_registro() {
        Usuario donoDoRegistro = Usuario.builder().id(1L).role(UserRole.PACIENTE).build();
        Paciente paciente = Paciente.builder().id(10L).usuario(donoDoRegistro).telefone("81999999999").build();

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        autenticarComo(donoDoRegistro);

        PacienteResponse response = pacienteService.buscarPorIdComOwnership(10L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void PACIENTE_nao_deve_conseguir_buscar_registro_de_outro_paciente() {
        Usuario donoDoRegistro = Usuario.builder().id(1L).role(UserRole.PACIENTE).build();
        Usuario atacante = Usuario.builder().id(2L).role(UserRole.PACIENTE).build();
        Paciente pacienteDeOutraPessoa = Paciente.builder().id(10L).usuario(donoDoRegistro).telefone("81999999999").build();

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteDeOutraPessoa));
        autenticarComo(atacante);

        assertThatThrownBy(() -> pacienteService.buscarPorIdComOwnership(10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ADMIN_deve_conseguir_buscar_qualquer_paciente() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PACIENTE).build();
        Usuario admin = Usuario.builder().id(99L).role(UserRole.ADMIN).build();
        Paciente paciente = Paciente.builder().id(10L).usuario(dono).telefone("81999999999").build();

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        autenticarComo(admin);

        PacienteResponse response = pacienteService.buscarPorIdComOwnership(10L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void PROFISSIONAL_deve_conseguir_buscar_qualquer_paciente() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PACIENTE).build();
        Usuario profissional = Usuario.builder().id(50L).role(UserRole.PROFISSIONAL).build();
        Paciente paciente = Paciente.builder().id(10L).usuario(dono).telefone("81999999999").build();

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        autenticarComo(profissional);

        PacienteResponse response = pacienteService.buscarPorIdComOwnership(10L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void PACIENTE_nao_deve_conseguir_atualizar_registro_de_outro_paciente() {
        Usuario dono = Usuario.builder().id(1L).role(UserRole.PACIENTE).build();
        Usuario atacante = Usuario.builder().id(2L).role(UserRole.PACIENTE).build();
        Paciente paciente = Paciente.builder().id(10L).usuario(dono).telefone("81999999999").build();

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(paciente));
        autenticarComo(atacante);

        assertThatThrownBy(() -> pacienteService.atualizarComOwnership(10L, new PacienteRequest(1L, "81900000000")))
                .isInstanceOf(AccessDeniedException.class);
    }
}