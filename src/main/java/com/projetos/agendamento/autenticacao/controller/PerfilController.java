package com.projetos.agendamento.autenticacao.controller;

import com.projetos.agendamento.autenticacao.dto.UsuarioMapper;
import com.projetos.agendamento.autenticacao.dto.UsuarioResponse;
import com.projetos.agendamento.autenticacao.security.AutenticacaoUtils;
import com.projetos.agendamento.paciente.dto.PacienteResponse;
import com.projetos.agendamento.paciente.service.PacienteService;
import com.projetos.agendamento.profissional.dto.ProfissionalResponse;
import com.projetos.agendamento.profissional.service.ProfissionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class PerfilController {

    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponse meuPerfil() {
        return UsuarioMapper.toResponse(AutenticacaoUtils.usuarioAutenticado());
    }

    @GetMapping("/paciente")
    @PreAuthorize("isAuthenticated()")
    public PacienteResponse meuCadastroPaciente() {
        return pacienteService.buscarMeuCadastro();
    }

    @GetMapping("/profissional")
    @PreAuthorize("isAuthenticated()")
    public ProfissionalResponse meuCadastroProfissional() {
        return profissionalService.buscarMeuCadastro();
    }
}