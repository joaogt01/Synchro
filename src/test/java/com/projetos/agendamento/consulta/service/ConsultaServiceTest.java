package com.projetos.agendamento.consulta.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.consulta.dto.ConsultaRequest;
import com.projetos.agendamento.consulta.dto.ConsultaResponse;
import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.entity.StatusAgendamento;
import com.projetos.agendamento.consulta.repository.ConsultaRepository;
import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.paciente.repository.PacienteRepository;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private OrquestradorValidacaoAgendamento orquestradorValidacaoAgendamento;

    @InjectMocks
    private ConsultaService consultaService;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Profissional profissionalComUsuario(Long profissionalId, Usuario usuario) {
        return Profissional.builder().id(profissionalId).usuario(usuario).especialidade("Cardiologia").build();
    }

    private Paciente pacienteComUsuario(Long pacienteId, Usuario usuario) {
        return Paciente.builder().id(pacienteId).usuario(usuario).telefone("81999999999").build();
    }

    @Test
    void ADMIN_deve_conseguir_criar_consulta_para_qualquer_profissional() {
        Usuario admin = Usuario.builder().id(1L).role(UserRole.ADMIN).build();
        Usuario donoDoProfissional = Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = profissionalComUsuario(10L, donoDoProfissional);
        Paciente paciente = pacienteComUsuario(20L, Usuario.builder().id(3L).role(UserRole.PACIENTE).build());

        autenticarComo(admin);
        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));
        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> {
            Consulta c = invocation.getArgument(0);
            c.setId(100L);
            c.setCriadoEm(LocalDateTime.now());
            return c;
        });

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(10L, 20L, inicio, inicio.plusMinutes(30));

        ConsultaResponse response = consultaService.criar(request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(StatusAgendamento.AGENDADO);
        verify(orquestradorValidacaoAgendamento).validarTodos(any(Consulta.class));
    }

    @Test
    void PROFISSIONAL_nao_deve_conseguir_criar_consulta_para_outro_profissional() {
        Usuario profissionalAutenticado = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Usuario donoDoProfissionalAlvo = Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build();
        Profissional profissionalAlvo = profissionalComUsuario(10L, donoDoProfissionalAlvo);

        autenticarComo(profissionalAutenticado);
        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissionalAlvo));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(10L, 20L, inicio, inicio.plusMinutes(30));

        assertThatThrownBy(() -> consultaService.criar(request))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(orquestradorValidacaoAgendamento);
    }

    @Test
    void PROFISSIONAL_deve_conseguir_criar_consulta_para_si_mesmo() {
        Usuario profissionalAutenticado = Usuario.builder().id(1L).role(UserRole.PROFISSIONAL).build();
        Profissional profissional = profissionalComUsuario(10L, profissionalAutenticado);
        Paciente paciente = pacienteComUsuario(20L, Usuario.builder().id(3L).role(UserRole.PACIENTE).build());

        autenticarComo(profissionalAutenticado);
        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));
        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(10L, 20L, inicio, inicio.plusMinutes(30));

        ConsultaResponse response = consultaService.criar(request);

        assertThat(response.profissionalId()).isEqualTo(10L);
    }

    @Test
    void deve_lancar_conflito_quando_validador_reprova_a_consulta() {
        Usuario admin = Usuario.builder().id(1L).role(UserRole.ADMIN).build();
        Profissional profissional = profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build());
        Paciente paciente = pacienteComUsuario(20L, Usuario.builder().id(3L).role(UserRole.PACIENTE).build());

        autenticarComo(admin);
        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));
        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        doThrow(new IllegalArgumentException("Horário fora da disponibilidade do profissional"))
                .when(orquestradorValidacaoAgendamento).validarTodos(any(Consulta.class));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(10L, 20L, inicio, inicio.plusMinutes(30));

        assertThatThrownBy(() -> consultaService.criar(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deve_traduzir_violacao_de_constraint_unica_em_409() {
        Usuario admin = Usuario.builder().id(1L).role(UserRole.ADMIN).build();
        Profissional profissional = profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build());
        Paciente paciente = pacienteComUsuario(20L, Usuario.builder().id(3L).role(UserRole.PACIENTE).build());

        autenticarComo(admin);
        when(profissionalRepository.findById(10L)).thenReturn(Optional.of(profissional));
        when(pacienteRepository.findById(20L)).thenReturn(Optional.of(paciente));
        when(consultaRepository.save(any(Consulta.class))).thenThrow(new DataIntegrityViolationException("uq_profissional_slot"));

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(10L, 20L, inicio, inicio.plusMinutes(30));

        assertThatThrownBy(() -> consultaService.criar(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Horário já ocupado");
    }

    @Test
    void deve_lancar_not_found_quando_profissional_nao_existe() {
        autenticarComo(Usuario.builder().id(1L).role(UserRole.ADMIN).build());
        when(profissionalRepository.findById(99L)).thenReturn(Optional.empty());

        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        ConsultaRequest request = new ConsultaRequest(99L, 20L, inicio, inicio.plusMinutes(30));

        assertThatThrownBy(() -> consultaService.criar(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ATENDENTE_deve_conseguir_confirmar_consulta_AGENDADA() {
        Consulta consulta = Consulta.builder().id(1L).status(StatusAgendamento.AGENDADO)
                .profissional(profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build()))
                .build();

        autenticarComo(Usuario.builder().id(5L).role(UserRole.ATENDENTE).build());
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        ConsultaResponse response = consultaService.confirmar(1L);

        assertThat(response.status()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

    @Test
    void PACIENTE_nao_deve_conseguir_confirmar_consulta() {
        Consulta consulta = Consulta.builder().id(1L).status(StatusAgendamento.AGENDADO)
                .profissional(profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build()))
                .build();

        autenticarComo(Usuario.builder().id(3L).role(UserRole.PACIENTE).build());
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.confirmar(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void nao_deve_confirmar_consulta_que_ja_foi_cancelada() {
        Consulta consulta = Consulta.builder().id(1L).status(StatusAgendamento.CANCELADO)
                .profissional(profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build()))
                .build();

        autenticarComo(Usuario.builder().id(9L).role(UserRole.ADMIN).build());
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.confirmar(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ATENDENTE_nao_deve_conseguir_completar_consulta() {
        Consulta consulta = Consulta.builder().id(1L).status(StatusAgendamento.CONFIRMADO)
                .profissional(profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build()))
                .build();

        autenticarComo(Usuario.builder().id(5L).role(UserRole.ATENDENTE).build());
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.completar(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void PROFISSIONAL_dono_deve_conseguir_completar_consulta_CONFIRMADA() {
        Usuario donoUsuario = Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build();
        Consulta consulta = Consulta.builder().id(1L).status(StatusAgendamento.CONFIRMADO)
                .profissional(profissionalComUsuario(10L, donoUsuario))
                .build();

        autenticarComo(donoUsuario);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        ConsultaResponse response = consultaService.completar(1L);

        assertThat(response.status()).isEqualTo(StatusAgendamento.COMPLETO);
    }

    @Test
    void ADMIN_deve_conseguir_cancelar_qualquer_consulta_nao_finalizada() {
        Consulta consulta = Consulta.builder().id(1L).status(StatusAgendamento.CONFIRMADO)
                .profissional(profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build()))
                .build();

        autenticarComo(Usuario.builder().id(9L).role(UserRole.ADMIN).build());
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        ConsultaResponse response = consultaService.cancelar(1L);

        assertThat(response.status()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    void nao_deve_cancelar_consulta_ja_completa() {
        Consulta consulta = Consulta.builder().id(1L).status(StatusAgendamento.COMPLETO)
                .profissional(profissionalComUsuario(10L, Usuario.builder().id(2L).role(UserRole.PROFISSIONAL).build()))
                .build();

        autenticarComo(Usuario.builder().id(9L).role(UserRole.ADMIN).build());
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.cancelar(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void PACIENTE_deve_ter_filtro_de_pacienteId_forcado_para_si_mesmo_na_listagem() {
        Usuario usuarioPaciente = Usuario.builder().id(3L).role(UserRole.PACIENTE).build();
        Paciente paciente = pacienteComUsuario(20L, usuarioPaciente);

        autenticarComo(usuarioPaciente);
        when(pacienteRepository.findByUsuarioId(3L)).thenReturn(Optional.of(paciente));
        when(consultaRepository.buscarComFiltros(null, 20L, null, null)).thenReturn(java.util.List.of());

        consultaService.listarComFiltros(999L, null, null, null); // tenta forçar outro profissionalId, mas isso é ignorado

        verify(consultaRepository).buscarComFiltros(null, 20L, null, null);
    }
}