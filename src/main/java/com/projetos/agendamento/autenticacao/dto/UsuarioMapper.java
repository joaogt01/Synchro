package com.projetos.agendamento.autenticacao.dto;

import com.projetos.agendamento.autenticacao.entity.Usuario;

public class UsuarioMapper {

    private UsuarioMapper() {}

    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }
}