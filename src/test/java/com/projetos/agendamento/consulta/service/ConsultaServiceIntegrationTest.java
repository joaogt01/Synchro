package com.projetos.agendamento.consulta.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.consulta.dto.ConsultaRequest;
import com.projetos.agendamento.consulta.dto.ConsultaResponse;
import com.projetos.agendamento.consulta.entity.StatusAgendamento;
import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.paciente.repository.PacienteRepository;
import com.projetos.agendamento.profissional.entity.Disponibilidade;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.DisponibilidadeRepository;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class ConsultaServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registrar) {
        registrar.add("spring.datasource.url", postgres::getJdbcUrl);
        registrar.add("spring.datasource.username", postgres::getUsername);
        registrar.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private DisponibilidadeRepository disponibilidadeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuarioProfissional;
    private Profissional profissional;
    private Paciente paciente;
    private LocalDateTime proximaSegundaAs10h;

    @BeforeEach
    void setUp() {
        usuarioProfissional = usuarioRepository.save(Usuario.builder()
                .nome("Dra. Ana")
                .email("ana.integracao@example.com")
                .senha(passwordEncoder.encode("senha12345"))
                .role(UserRole.PROFISSIONAL)
                .build());

        profissional = profissionalRepository.save(Profissional.builder()
                .usuario(usuarioProfissional)
                .especialidade("Cardiologia")
                .ativo(true)
                .build());

        Usuario usuarioPaciente = usuarioRepository.save(Usuario.builder()
                .nome("João")
                .email("joao.integracao@example.com")
                .senha(passwordEncoder.encode("senha12345"))
                .role(UserRole.PACIENTE)
                .build());

        paciente = pacienteRepository.save(Paciente.builder()
                .usuario(usuarioPaciente)
                .telefone("81999999999")
                .build());

        proximaSegundaAs10h = LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        disponibilidadeRepository.save(Disponibilidade.builder()
                .profissional(profissional)
                .diaDaSemana((short) DayOfWeek.MONDAY.getValue())
                .inicio(LocalTime.of(8, 0))
                .fim(LocalTime.of(18, 0))
                .build());
    }

    private void autenticarComo(Usuario usuario) {
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void deve_criar_confirmar_e_completar_uma_consulta_de_ponta_a_ponta() {
        Usuario admin = Usuario.builder().id(-1L).role(UserRole.ADMIN).build();
        autenticarComo(admin);

        ConsultaRequest request = new ConsultaRequest(
                profissional.getId(),
                paciente.getId(),
                proximaSegundaAs10h,
                proximaSegundaAs10h.plusMinutes(30)
        );

        ConsultaResponse criada = consultaService.criar(request);
        assertThat(criada.status()).isEqualTo(StatusAgendamento.AGENDADO);

        ConsultaResponse confirmada = consultaService.confirmar(criada.id());
        assertThat(confirmada.status()).isEqualTo(StatusAgendamento.CONFIRMADO);

        autenticarComo(usuarioProfissional);
        ConsultaResponse completada = consultaService.completar(criada.id());
        assertThat(completada.status()).isEqualTo(StatusAgendamento.COMPLETO);
    }

    @Test
    void nao_deve_permitir_duas_consultas_no_mesmo_horario_para_o_mesmo_profissional() {
        autenticarComo(Usuario.builder().id(-1L).role(UserRole.ADMIN).build());

        Usuario outroUsuarioPaciente = usuarioRepository.save(Usuario.builder()
                .nome("Maria")
                .email("maria.integracao@example.com")
                .senha(passwordEncoder.encode("senha12345"))
                .role(UserRole.PACIENTE)
                .build());
        Paciente outroPaciente = pacienteRepository.save(Paciente.builder()
                .usuario(outroUsuarioPaciente)
                .telefone("81988888888")
                .build());

        ConsultaRequest primeira = new ConsultaRequest(
                profissional.getId(), paciente.getId(), proximaSegundaAs10h, proximaSegundaAs10h.plusMinutes(30));
        consultaService.criar(primeira);

        ConsultaRequest segundaNoMesmoHorario = new ConsultaRequest(
                profissional.getId(), outroPaciente.getId(), proximaSegundaAs10h, proximaSegundaAs10h.plusMinutes(30));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.criar(segundaNoMesmoHorario)
        );
    }

    @Test
    void nao_deve_permitir_agendar_fora_da_disponibilidade_do_profissional() {
        autenticarComo(Usuario.builder().id(-1L).role(UserRole.ADMIN).build());

        LocalDateTime foraDoHorario = proximaSegundaAs10h.withHour(23);
        ConsultaRequest request = new ConsultaRequest(
                profissional.getId(), paciente.getId(), foraDoHorario, foraDoHorario.plusMinutes(30));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.criar(request)
        );
    }
}