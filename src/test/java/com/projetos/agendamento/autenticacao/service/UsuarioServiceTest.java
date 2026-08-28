package com.projetos.agendamento.autenticacao.service;

import com.projetos.agendamento.autenticacao.dto.NovoUsuarioPrivilegiadoRequest;
import com.projetos.agendamento.autenticacao.dto.RegistroRequest;
import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void auto_registro_sempre_cria_conta_com_role_PACIENTE() {
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha12345")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroRequest request = new RegistroRequest("João", "joao@example.com", "senha12345");

        Usuario usuario = usuarioService.registrar(request);

        assertThat(usuario.getRole()).isEqualTo(UserRole.PACIENTE);
    }

    @Test
    void nao_deve_permitir_registro_publico_duplicado_por_email() {
        when(usuarioRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(Usuario.builder().build()));

        RegistroRequest request = new RegistroRequest("João", "joao@example.com", "senha12345");

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail já cadastrado");
    }

    @Test
    void admin_deve_conseguir_criar_conta_ATENDENTE_via_criacao_privilegiada() {
        when(usuarioRepository.findByEmail("recepcao@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha12345")).thenReturn("hash");
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        when(usuarioRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        NovoUsuarioPrivilegiadoRequest request =
                new NovoUsuarioPrivilegiadoRequest("Recepção", "recepcao@example.com", "senha12345", UserRole.ATENDENTE);

        usuarioService.criarComRolePrivilegiado(request);

        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ATENDENTE);
    }

    @Test
    void nao_deve_permitir_criar_PROFISSIONAL_via_criacao_privilegiada() {
        NovoUsuarioPrivilegiadoRequest request =
                new NovoUsuarioPrivilegiadoRequest("Dr. João", "joao@example.com", "senha12345", UserRole.PROFISSIONAL);

        assertThatThrownBy(() -> usuarioService.criarComRolePrivilegiado(request))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void nao_deve_permitir_criar_PACIENTE_via_criacao_privilegiada() {
        NovoUsuarioPrivilegiadoRequest request =
                new NovoUsuarioPrivilegiadoRequest("João", "joao@example.com", "senha12345", UserRole.PACIENTE);

        assertThatThrownBy(() -> usuarioService.criarComRolePrivilegiado(request))
                .isInstanceOf(ResponseStatusException.class);
    }
}