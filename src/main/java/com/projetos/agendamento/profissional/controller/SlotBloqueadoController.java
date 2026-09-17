package com.projetos.agendamento.profissional.controller;

import com.projetos.agendamento.profissional.dto.SlotBloqueadoRequest;
import com.projetos.agendamento.profissional.dto.SlotBloqueadoResponse;
import com.projetos.agendamento.profissional.service.SlotBloqueadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais/{profissionalId}/slots-bloqueados")
@RequiredArgsConstructor
public class SlotBloqueadoController {

    private final SlotBloqueadoService slotBloqueadoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public SlotBloqueadoResponse cadastrar(@PathVariable Long profissionalId, @Valid @RequestBody SlotBloqueadoRequest request) {
        return slotBloqueadoService.cadastrarComOwnership(profissionalId, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<SlotBloqueadoResponse> listar(@PathVariable Long profissionalId) {
        return slotBloqueadoService.listarPorProfissional(profissionalId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public void deletar(@PathVariable Long profissionalId, @PathVariable Long id) {
        slotBloqueadoService.deletarComOwnership(profissionalId, id);
    }
}