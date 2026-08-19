package com.projetos.agendamento.autenticacao.dto;

import com.projetos.agendamento.autenticacao.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "senha deve ter no mínimo 8 caracteres") String senha,
        @NotNull UserRole role
) {}