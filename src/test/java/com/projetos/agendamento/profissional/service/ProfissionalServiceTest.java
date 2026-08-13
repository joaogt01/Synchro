package com.projetos.agendamento.profissional.service;
import com.projetos.agendamento.autenticacao.entity.Usuario;


import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.profissional.dto.ProfissionalRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalResponse;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
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
class ProfessionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

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
}