package com.projetos.agendamento.profissional.controller;

import com.projetos.agendamento.profissional.dto.DisponibilidadeRequest;
import com.projetos.agendamento.profissional.dto.DisponibilidadeResponse;
import com.projetos.agendamento.profissional.service.DisponibilidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais/{profissionalId}/disponibilidade")
@RequiredArgsConstructor
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public DisponibilidadeResponse cadastrar(@PathVariable Long profissionalId, @Valid @RequestBody DisponibilidadeRequest request) {
        return disponibilidadeService.cadastrarComOwnership(profissionalId, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<DisponibilidadeResponse> listar(@PathVariable Long profissionalId) {
        return disponibilidadeService.listarPorProfissional(profissionalId);
    }
}