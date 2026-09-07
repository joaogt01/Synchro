package com.projetos.agendamento.consulta.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.consulta.dto.ConsultaRequest;
import com.projetos.agendamento.consulta.evento.ConsultaAgendadaEvento;
import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.paciente.repository.PacienteRepository;
import com.projetos.agendamento.profissional.entity.Disponibilidade;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.DisponibilidadeRepository;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@RecordApplicationEvents
@Transactional
class ConsultaAgendamentoEventoIntegracaoTest {

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

    @Autowired
    private ApplicationEvents applicationEvents;

    private Profissional profissional;
    private Paciente paciente;
    private LocalDateTime proximaSegundaAs10h;

    @BeforeEach
    void setUp() {
        Usuario usuarioProfissional = usuarioRepository.save(Usuario.builder()
                .nome("Dra. Ana").email("ana.evento@example.com")
                .senha(passwordEncoder.encode("senha12345")).role(UserRole.PROFISSIONAL).build());

        profissional = profissionalRepository.save(Profissional.builder()
                .usuario(usuarioProfissional).especialidade("Cardiologia").ativo(true).build());

        Usuario usuarioPaciente = usuarioRepository.save(Usuario.builder()
                .nome("João").email("joao.evento@example.com")
                .senha(passwordEncoder.encode("senha12345")).role(UserRole.PACIENTE).build());

        paciente = pacienteRepository.save(Paciente.builder()
                .usuario(usuarioPaciente).telefone("81999999999").build());

        proximaSegundaAs10h = LocalDateTime.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        disponibilidadeRepository.save(Disponibilidade.builder()
                .profissional(profissional)
                .diaDaSemana((short) DayOfWeek.MONDAY.getValue())
                .inicio(LocalTime.of(8, 0)).fim(LocalTime.of(18, 0)).build());

        autenticarComoAdmin();
    }

    private void autenticarComoAdmin() {
        var admin = Usuario.builder().id(-1L).role(UserRole.ADMIN).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));
    }

    @Test
    @DisplayName("Deve publicar evento de consulta agendada após criação")
    void deve_publicar_evento_e_acionar_listener_de_notificacao_apos_agendar() {
        ConsultaRequest request = new ConsultaRequest(
                profissional.getId(), paciente.getId(),
                proximaSegundaAs10h, proximaSegundaAs10h.plusMinutes(90));

        var response = consultaService.criar(request);

        assertThat(response.id()).isNotNull();

        assertThat(applicationEvents.stream(ConsultaAgendadaEvento.class))
                .hasSize(1)
                .anySatisfy(evento -> assertThat(evento.consultaId()).isEqualTo(response.id()));
    }

    @Test
    @DisplayName("Deve permitir agendar consultas consecutivas quando os horários não se sobrepõem")
    void deve_permitir_consultas_consecutivas_sem_sobreposicao_de_duracao_flexivel() {
        Paciente outroPaciente = criarOutroPaciente("Maria", "maria.flex@example.com", "81988888888");

        ConsultaRequest primeira = new ConsultaRequest(
                profissional.getId(), paciente.getId(),
                proximaSegundaAs10h, proximaSegundaAs10h.plusMinutes(90));
        consultaService.criar(primeira);

        ConsultaRequest segunda = new ConsultaRequest(
                profissional.getId(), outroPaciente.getId(),
                proximaSegundaAs10h.plusMinutes(90), proximaSegundaAs10h.plusMinutes(120));

        assertDoesNotThrow(() -> consultaService.criar(segunda));
    }

    @Test
    @DisplayName("Não deve permitir agendar consultas com sobreposição parcial de horário")
    void nao_deve_permitir_consulta_com_sobreposicao_parcial_de_duracao_flexivel() {
        Paciente outroPaciente = criarOutroPaciente("Maria", "maria.flex2@example.com", "81977777777");

        ConsultaRequest primeira = new ConsultaRequest(
                profissional.getId(), paciente.getId(),
                proximaSegundaAs10h, proximaSegundaAs10h.plusMinutes(60));
        consultaService.criar(primeira);

        ConsultaRequest sobreposta = new ConsultaRequest(
                profissional.getId(), outroPaciente.getId(),
                proximaSegundaAs10h.plusMinutes(30), proximaSegundaAs10h.plusMinutes(90));

        assertThrows(
                IllegalArgumentException.class,
                () -> consultaService.criar(sobreposta));
    }

    private Paciente criarOutroPaciente(String nome, String email, String telefone) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(nome).email(email)
                .senha(passwordEncoder.encode("senha12345")).role(UserRole.PACIENTE).build());

        return pacienteRepository.save(Paciente.builder()
                .usuario(usuario).telefone(telefone).build());
    }
}