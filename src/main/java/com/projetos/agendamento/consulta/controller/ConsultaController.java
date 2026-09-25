package com.projetos.agendamento.consulta.controller;

import com.projetos.agendamento.consulta.dto.ConsultaHistoricoStatusResponse;
import com.projetos.agendamento.consulta.dto.ConsultaRequest;
import com.projetos.agendamento.consulta.dto.ConsultaResponse;
import com.projetos.agendamento.consulta.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'PROFISSIONAL')")
    public ConsultaResponse criar(@Valid @RequestBody ConsultaRequest request) {
        return consultaService.criar(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ConsultaResponse buscarPorId(@PathVariable Long id) {
        return consultaService.buscarPorIdComOwnership(id);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<ConsultaResponse> listar(
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @PageableDefault(size = 20, sort = "inicio") Pageable pageable
    ) {
        return consultaService.listarComFiltros(profissionalId, pacienteId, inicio, fim, pageable);
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'PROFISSIONAL')")
    public ConsultaResponse confirmar(@PathVariable Long id) {
        return consultaService.confirmar(id);
    }

    @PostMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public ConsultaResponse completar(@PathVariable Long id) {
        return consultaService.completar(id);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public ConsultaResponse cancelar(@PathVariable Long id) {
        return consultaService.cancelar(id);
    }

    @PatchMapping("/{id}/reagendar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFISSIONAL')")
    public ConsultaResponse reagendar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest request) {
        return consultaService.reagendar(id, request);
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("isAuthenticated()")
    public List<ConsultaHistoricoStatusResponse> historico(@PathVariable Long id) {
        return consultaService.listarHistorico(id);
    }
}