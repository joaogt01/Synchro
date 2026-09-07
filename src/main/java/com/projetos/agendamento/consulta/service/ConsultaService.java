package com.projetos.agendamento.consulta.service;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.security.AutenticacaoUtils;
import com.projetos.agendamento.consulta.dto.ConsultaMapper;
import com.projetos.agendamento.consulta.dto.ConsultaRequest;
import com.projetos.agendamento.consulta.dto.ConsultaResponse;
import com.projetos.agendamento.consulta.entity.Consulta;
import com.projetos.agendamento.consulta.entity.StatusAgendamento;
<<<<<<< HEAD
=======
import com.projetos.agendamento.consulta.evento.ConsultaAgendadaEvento;
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)
import com.projetos.agendamento.consulta.repository.ConsultaRepository;
import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.paciente.repository.PacienteRepository;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======
import org.springframework.context.ApplicationEventPublisher;
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final OrquestradorValidacaoAgendamento orquestradorValidacaoAgendamento;
<<<<<<< HEAD
=======
    private final ApplicationEventPublisher eventPublisher;
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)

    @Transactional
    public ConsultaResponse criar(ConsultaRequest request) {
        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado: " + request.profissionalId()));

        exigirProfissionalPodeAgendar(profissional);

        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + request.pacienteId()));

        Consulta consulta = Consulta.builder()
                .profissional(profissional)
                .paciente(paciente)
                .inicio(request.inicio())
                .fim(request.fim())
                .status(StatusAgendamento.AGENDADO)
                .build();

        orquestradorValidacaoAgendamento.validarTodos(consulta);

        try {
<<<<<<< HEAD
            return ConsultaMapper.toResponse(consultaRepository.save(consulta));
=======
            Consulta salva = consultaRepository.save(consulta);

            eventPublisher.publishEvent(new ConsultaAgendadaEvento(
                    salva.getId(),
                    salva.getProfissional().getId(),
                    salva.getProfissional().getUsuario().getNome(),
                    salva.getPaciente().getId(),
                    salva.getPaciente().getUsuario().getNome(),
                    salva.getInicio(),
                    salva.getFim()
            ));

            return ConsultaMapper.toResponse(salva);
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário já ocupado para este profissional");
        }
    }

    @Transactional(readOnly = true)
    public ConsultaResponse buscarPorIdComOwnership(Long id) {
        Consulta consulta = getOrThrow(id);
        exigirVisualizacaoPermitida(consulta);
        return ConsultaMapper.toResponse(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listarComFiltros(Long profissionalId, Long pacienteId, LocalDateTime inicio, LocalDateTime fim) {
        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();

        if (usuarioAutenticado.getRole() == UserRole.PACIENTE) {
            pacienteId = pacienteRepository.findByUsuarioId(usuarioAutenticado.getId())
                    .map(Paciente::getId)
                    .orElseThrow(() -> new IllegalStateException("Usuário autenticado não possui cadastro de paciente"));
        } else if (usuarioAutenticado.getRole() == UserRole.PROFISSIONAL) {
            profissionalId = profissionalRepository.findByUsuarioId(usuarioAutenticado.getId())
                    .map(Profissional::getId)
                    .orElseThrow(() -> new IllegalStateException("Usuário autenticado não possui cadastro de profissional"));
        }

        return consultaRepository.buscarComFiltros(profissionalId, pacienteId, inicio, fim).stream()
                .map(ConsultaMapper::toResponse)
                .toList();
    }

    @Transactional
    public ConsultaResponse confirmar(Long id) {
        Consulta consulta = getOrThrow(id);
        exigirDonoAtendenteOuAdmin(consulta);
        exigirStatusAtual(consulta, StatusAgendamento.AGENDADO);
        consulta.setStatus(StatusAgendamento.CONFIRMADO);
        return ConsultaMapper.toResponse(consulta);
    }

    @Transactional
    public ConsultaResponse completar(Long id) {
        Consulta consulta = getOrThrow(id);
        exigirDonoOuAdmin(consulta);
        exigirStatusAtual(consulta, StatusAgendamento.CONFIRMADO);
        consulta.setStatus(StatusAgendamento.COMPLETO);
        return ConsultaMapper.toResponse(consulta);
    }

    @Transactional
    public ConsultaResponse cancelar(Long id) {
        Consulta consulta = getOrThrow(id);
        exigirDonoOuAdmin(consulta);
        if (consulta.getStatus() == StatusAgendamento.COMPLETO || consulta.getStatus() == StatusAgendamento.CANCELADO) {
            throw new IllegalStateException("Não é possível cancelar uma consulta que já está " + consulta.getStatus());
        }
        consulta.setStatus(StatusAgendamento.CANCELADO);
        return ConsultaMapper.toResponse(consulta);
    }

    private Consulta getOrThrow(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada: " + id));
    }

    private void exigirStatusAtual(Consulta consulta, StatusAgendamento esperado) {
        if (consulta.getStatus() != esperado) {
            throw new IllegalStateException(
                    "Consulta está em status " + consulta.getStatus() + ", era esperado " + esperado);
        }
    }

    private void exigirProfissionalPodeAgendar(Profissional profissional) {
        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();
        if (usuarioAutenticado.getRole() != UserRole.PROFISSIONAL) {
<<<<<<< HEAD
            return; // ADMIN e ATENDENTE podem agendar para qualquer profissional
=======
            return;
>>>>>>> dd794b5 (feat: adiciona ConsultaService e sistema de eventos de agendamento)
        }
        boolean ehOProprioProfissional = profissional.getUsuario().getId().equals(usuarioAutenticado.getId());
        if (!ehOProprioProfissional) {
            throw new AccessDeniedException("Profissional só pode agendar consultas para si mesmo");
        }
    }

    private boolean ehProfissionalDono(Consulta consulta, Usuario usuarioAutenticado) {
        return usuarioAutenticado.getRole() == UserRole.PROFISSIONAL
                && consulta.getProfissional().getUsuario().getId().equals(usuarioAutenticado.getId());
    }

    private void exigirDonoAtendenteOuAdmin(Consulta consulta) {
        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();
        boolean permitido = usuarioAutenticado.getRole() == UserRole.ADMIN
                || usuarioAutenticado.getRole() == UserRole.ATENDENTE
                || ehProfissionalDono(consulta, usuarioAutenticado);
        if (!permitido) {
            throw new AccessDeniedException("Apenas o profissional responsável, atendente ou admin podem confirmar esta consulta");
        }
    }

    private void exigirDonoOuAdmin(Consulta consulta) {
        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();
        boolean permitido = usuarioAutenticado.getRole() == UserRole.ADMIN
                || ehProfissionalDono(consulta, usuarioAutenticado);
        if (!permitido) {
            throw new AccessDeniedException("Apenas o profissional responsável ou admin podem realizar esta ação");
        }
    }

    private void exigirVisualizacaoPermitida(Consulta consulta) {
        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();
        boolean permitido = usuarioAutenticado.getRole() == UserRole.ADMIN
                || usuarioAutenticado.getRole() == UserRole.ATENDENTE
                || ehProfissionalDono(consulta, usuarioAutenticado)
                || consulta.getPaciente().getUsuario().getId().equals(usuarioAutenticado.getId());
        if (!permitido) {
            throw new AccessDeniedException("Acesso negado a esta consulta");
        }
    }
}