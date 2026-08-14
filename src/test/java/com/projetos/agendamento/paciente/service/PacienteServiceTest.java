package com.projetos.agendamento.paciente.service;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.paciente.dto.PacienteRequest;
import com.projetos.agendamento.paciente.dto.PacienteResponse;
import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.paciente.repository.PacienteRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        assertThatThrownBy(() -> pacienteService.buscarPorId(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
