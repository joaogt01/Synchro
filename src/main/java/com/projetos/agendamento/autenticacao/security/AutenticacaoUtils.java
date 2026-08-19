package com.projetos.agendamento.autenticacao.security;

import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.entity.UserRole;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AutenticacaoUtils {

    private AutenticacaoUtils() {
    }

    public static Usuario usuarioAutenticado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario usuario)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto de segurança");
        }
        return usuario;
    }

    public static Long usuarioIdAutenticado() {
        return usuarioAutenticado().getId();
    }

    public static boolean possuiRole(UserRole role) {
        return usuarioAutenticado().getRole() == role;
    }
}