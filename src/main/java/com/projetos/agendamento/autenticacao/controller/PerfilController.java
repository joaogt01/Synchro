package com.projetos.agendamento.autenticacao.controller;

import com.projetos.agendamento.autenticacao.dto.UsuarioMapper;
import com.projetos.agendamento.autenticacao.dto.UsuarioResponse;
import com.projetos.agendamento.autenticacao.security.AutenticacaoUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class PerfilController{

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponse meuPerfil() {
        return UsuarioMapper.toResponse(AutenticacaoUtils.usuarioAutenticado());
    }
}