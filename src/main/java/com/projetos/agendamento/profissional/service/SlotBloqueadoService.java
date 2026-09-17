package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.security.AutenticacaoUtils;
import com.projetos.agendamento.profissional.dto.SlotBloqueadoMapper;
import com.projetos.agendamento.profissional.dto.SlotBloqueadoRequest;
import com.projetos.agendamento.profissional.dto.SlotBloqueadoResponse;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.entity.SlotBloqueado;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.profissional.repository.SlotBloqueadoRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotBloqueadoService {

    private final SlotBloqueadoRepository slotBloqueadoRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional
    public SlotBloqueadoResponse cadastrarComOwnership(Long profissionalId, SlotBloqueadoRequest request) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado: " + profissionalId));

        exigirOwnership(profissional);

        validarIntervalo(request);
        validarSemSobreposicao(profissionalId, request);

        SlotBloqueado slotBloqueado = SlotBloqueado.builder()
                .profissional(profissional)
                .inicio(request.inicio())
                .fim(request.fim())
                .build();

        return SlotBloqueadoMapper.toResponse(slotBloqueadoRepository.save(slotBloqueado));
    }

    @Transactional(readOnly = true)
    public List<SlotBloqueadoResponse> listarPorProfissional(Long profissionalId) {
        return slotBloqueadoRepository.findByProfissionalId(profissionalId).stream()
                .map(SlotBloqueadoMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deletarComOwnership(Long profissionalId, Long id) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado: " + profissionalId));

        exigirOwnership(profissional);

        SlotBloqueado slotBloqueado = slotBloqueadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot bloqueado não encontrado: " + id));

        if (!slotBloqueado.getProfissional().getId().equals(profissionalId)) {
            throw new ResourceNotFoundException("Slot bloqueado não encontrado: " + id);
        }

        slotBloqueadoRepository.delete(slotBloqueado);
    }

    private void exigirOwnership(Profissional profissional) {
        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();
        boolean donoDoRecurso = profissional.getUsuario().getId().equals(usuarioAutenticado.getId());

        if (usuarioAutenticado.getRole() != UserRole.ADMIN && !donoDoRecurso) {
            throw new AccessDeniedException("Acesso negado: você não é o profissional dono desta agenda");
        }
    }

    private void validarIntervalo(SlotBloqueadoRequest request) {
        if (!request.inicio().isBefore(request.fim())) {
            throw new IllegalArgumentException("O horário de início deve ser anterior ao horário de término");
        }
        if (request.inicio().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível bloquear um horário no passado");
        }
    }

    private void validarSemSobreposicao(Long profissionalId, SlotBloqueadoRequest request) {
        boolean sobrepoe = slotBloqueadoRepository.findByProfissionalId(profissionalId).stream()
                .anyMatch(existente -> request.inicio().isBefore(existente.getFim()) && request.fim().isAfter(existente.getInicio()));

        if (sobrepoe) {
            throw new IllegalArgumentException("Horário sobreposto a um bloqueio já cadastrado");
        }
    }
}