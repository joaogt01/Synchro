package com.projetos.agendamento.profissional.controller;

import com.projetos.agendamento.profissional.dto.ProfissionalAtualizarRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalResponse;
import com.projetos.agendamento.profissional.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProfissionalResponse criar(@Valid @RequestBody ProfissionalRequest request) {
        return profissionalService.criar(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ProfissionalResponse buscarPorId(@PathVariable Long id) {
        return profissionalService.buscarPorId(id);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<ProfissionalResponse> buscarTodos(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return profissionalService.buscarTodos(pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public ProfissionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalAtualizarRequest request) {
        return profissionalService.atualizarComOwnership(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        profissionalService.delete(id);
    }
}