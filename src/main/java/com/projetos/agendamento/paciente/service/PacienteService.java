package com.projetos.agendamento.paciente.service;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.autenticacao.security.AutenticacaoUtils;
import com.projetos.agendamento.paciente.dto.PacienteMapper;
import com.projetos.agendamento.paciente.dto.PacienteRequest;
import com.projetos.agendamento.paciente.dto.PacienteResponse;
import com.projetos.agendamento.paciente.entity.Paciente;
import com.projetos.agendamento.paciente.repository.PacienteRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PacienteResponse criar(PacienteRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.usuarioId()));

        Paciente paciente = Paciente.builder()
                .usuario(usuario)
                .telefone(request.telefone())
                .build();

        return PacienteMapper.toResponse(pacienteRepository.save(paciente));
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorIdComOwnership(Long id) {
        Paciente paciente = buscarOuLancarErro(id);
        garantirOwnershipOuPapelPrivilegiado(paciente);
        return PacienteMapper.toResponse(paciente);
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listarTodos() {
        return pacienteRepository.findAll().stream()
                .map(PacienteMapper::toResponse)
                .toList();
    }

    @Transactional
    public PacienteResponse atualizarComOwnership(Long id, PacienteRequest request) {
        Paciente paciente = buscarOuLancarErro(id);
        garantirOwnershipOuPapelPrivilegiado(paciente);
        paciente.setTelefone(request.telefone());
        return PacienteMapper.toResponse(paciente);
    }

    @Transactional
    public void deletar(Long id) {
        Paciente paciente = buscarOuLancarErro(id);
        pacienteRepository.delete(paciente);
    }

    private Paciente buscarOuLancarErro(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado: " + id));
    }

    private void garantirOwnershipOuPapelPrivilegiado(Paciente paciente) {
        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();

        boolean papelPrivilegiado = usuarioAutenticado.getRole() == UserRole.ADMIN
                || usuarioAutenticado.getRole() == UserRole.PROFISSIONAL;

        boolean donoDoRecurso = paciente.getUsuario().getId().equals(usuarioAutenticado.getId());

        if (!papelPrivilegiado && !donoDoRecurso) {
            throw new AccessDeniedException("Acesso negado a este paciente");
        }
    }
}