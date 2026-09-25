package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.autenticacao.security.AutenticacaoUtils;
import com.projetos.agendamento.profissional.dto.ProfissionalAtualizarRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalMapper;
import com.projetos.agendamento.profissional.dto.ProfissionalRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalResponse;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(UserRole.PROFISSIONAL)
                .build();

        try {
            usuario = usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Profissional profissional = Profissional.builder()
                .usuario(usuario)
                .especialidade(request.especialidade())
                .ativo(true)
                .build();

        return ProfissionalMapper.toResponse(profissionalRepository.save(profissional));
    }

    @Transactional(readOnly = true)
    public ProfissionalResponse buscarPorId(Long id) {
        return ProfissionalMapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProfissionalResponse buscarMeuCadastro() {
        Long usuarioId = AutenticacaoUtils.usuarioIdAutenticado();
        Profissional profissional = profissionalRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não possui cadastro de profissional"));
        return ProfissionalMapper.toResponse(profissional);
    }

    @Transactional(readOnly = true)
    public Page<ProfissionalResponse> buscarTodos(Pageable pageable) {
        return profissionalRepository.findAll(pageable)
                .map(ProfissionalMapper::toResponse);
    }

    @Transactional
    public ProfissionalResponse atualizarComOwnership(Long id, ProfissionalAtualizarRequest request) {
        Profissional profissional = getOrThrow(id);

        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();
        boolean donoDoRecurso = profissional.getUsuario().getId().equals(usuarioAutenticado.getId());

        if (usuarioAutenticado.getRole() != UserRole.ADMIN && !donoDoRecurso) {
            throw new AccessDeniedException("Acesso negado a este profissional");
        }

        profissional.setEspecialidade(request.especialidade());
        return ProfissionalMapper.toResponse(profissional);
    }

    @Transactional
    public void delete(Long id) {
        Profissional profissional = getOrThrow(id);
        profissionalRepository.delete(profissional);
    }

    private Profissional getOrThrow(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado: " + id));
    }
}