package com.projetos.agendamento.paciente.controller;

import com.projetos.agendamento.paciente.dto.PacienteRequest;
import com.projetos.agendamento.paciente.dto.PacienteResponse;
import com.projetos.agendamento.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public PacienteResponse criar(@Valid @RequestBody PacienteRequest request) {
        return pacienteService.criar(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL', 'PACIENTE')")
    public PacienteResponse buscarPorId(@PathVariable Long id) {
        return pacienteService.buscarPorIdComOwnership(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public List<PacienteResponse> listarTodos() {
        return pacienteService.listarTodos();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
    public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return pacienteService.atualizarComOwnership(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletar(@PathVariable Long id) {
        pacienteService.deletar(id);
    }
}