package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import com.projetos.agendamento.profissional.dto.ProfissionalMapper;
import com.projetos.agendamento.profissional.dto.ProfissionalRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalResponse;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ProfissionalResponse criar(ProfissionalRequest request) {
        Usuario usuario = usuarioRepository.buscarPorId(request.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario não encontrado: " + request.idUsuario()));

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
    public List<ProfissionalResponse> buscarTodos() {
        return profissionalRepository.findAll().stream()
                .map(ProfissionalMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProfissionalResponse atualizar(Long id, ProfissionalRequest request) {
        Profissional profissional = getOrThrow(id);
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
