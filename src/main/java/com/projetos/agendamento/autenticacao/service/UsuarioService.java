package com.projetos.agendamento.autenticacao.service;

import com.projetos.agendamento.autenticacao.dto.NovoUsuarioPrivilegiadoRequest;
import com.projetos.agendamento.autenticacao.dto.RegistroRequest;
import com.projetos.agendamento.autenticacao.entity.UserRole;
import com.projetos.agendamento.autenticacao.entity.Usuario;
import com.projetos.agendamento.autenticacao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Set<UserRole> PAPEIS_CRIAVEIS_POR_ADMIN = EnumSet.of(UserRole.ADMIN, UserRole.ATENDENTE);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario registrar(RegistroRequest request) {
        return salvar(request.nome(), request.email(), request.senha(), UserRole.PACIENTE);
    }


    @Transactional
    public Usuario criarComRolePrivilegiado(NovoUsuarioPrivilegiadoRequest request) {
        if (!PAPEIS_CRIAVEIS_POR_ADMIN.contains(request.role())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Papel inválido para este endpoint. Use POST /api/profissionais para criar contas PROFISSIONAL."
            );
        }
        return salvar(request.nome(), request.email(), request.senha(), request.role());
    }

    private Usuario salvar(String nome, String email, String senha, UserRole role) {
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .role(role)
                .build();

        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
    }
}