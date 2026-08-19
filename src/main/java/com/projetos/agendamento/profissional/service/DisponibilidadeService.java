package com.projetos.agendamento.profissional.service;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.security.AutenticacaoUtils;
import com.projetos.agendamento.profissional.dto.DisponibilidadeMapper;
import com.projetos.agendamento.profissional.dto.DisponibilidadeRequest;
import com.projetos.agendamento.profissional.dto.DisponibilidadeResponse;
import com.projetos.agendamento.profissional.entity.Disponibilidade;
import com.projetos.agendamento.profissional.entity.Profissional;
import com.projetos.agendamento.profissional.repository.DisponibilidadeRepository;
import com.projetos.agendamento.profissional.repository.ProfissionalRepository;
import com.projetos.agendamento.utils.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadeService {

    private final DisponibilidadeRepository disponibilidadeRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional
    public DisponibilidadeResponse cadastrarComOwnership(Long profissionalId, DisponibilidadeRequest request){
        Profissional profissional = profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

        Usuario usuarioAutenticado = AutenticacaoUtils.usuarioAutenticado();
        boolean donoDoRecurso = profissional.getUsuario().getId().equals(usuarioAutenticado.getId());

        if (usuarioAutenticado.getRole() != UserRole.ADMIN && !donoDoRecurso) {
            throw new AccessDeniedException("Acesso negado: você não é o profissional dono desta agenda");
        }

        validarSemSobreposicao(profissionalId, request);

        Disponibilidade disponibilidade = Disponibilidade.builder()
                .profissional(profissional)
                .diaDaSemana(request.diaDaSemana())
                .inicio(request.horaInicio())
                .fim(request.horaFim())
                .build();
        return DisponibilidadeMapper.toResponse(disponibilidadeRepository.save(disponibilidade));
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeResponse> listarPorProfissional(Long profissionalId){
        return disponibilidadeRepository.findByProfissionalId(profissionalId).stream()
                .map(DisponibilidadeMapper::toResponse)
                .toList();
    }

    private void validarSemSobreposicao(Long profissionalId, DisponibilidadeRequest request){
        boolean sobrepoe = disponibilidadeRepository.findByProfissionalId(profissionalId).stream()
                .filter(disponibilidade -> disponibilidade.getDiaDaSemana().equals(request.diaDaSemana()))
                .anyMatch(disponibilidade -> request.horaInicio().isBefore(disponibilidade.getFim()) && request.horaFim().isAfter(disponibilidade.getInicio()));

        if (sobrepoe){throw new IllegalArgumentException("Horário sobreposto a uma disponibilidade já cadastrada");
        }
    }
}