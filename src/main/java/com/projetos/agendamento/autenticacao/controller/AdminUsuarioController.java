package com.projetos.agendamento.autenticacao.controller;

import com.projetos.agendamento.autenticacao.dto.NovoUsuarioPrivilegiadoRequest;
import com.projetos.agendamento.autenticacao.dto.UsuarioMapper;
import com.projetos.agendamento.autenticacao.dto.UsuarioResponse;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse criar(@Valid @RequestBody NovoUsuarioPrivilegiadoRequest request) {
        Usuario usuario = usuarioService.criarComRolePrivilegiado(request);
        return UsuarioMapper.toResponse(usuario);
    }
}