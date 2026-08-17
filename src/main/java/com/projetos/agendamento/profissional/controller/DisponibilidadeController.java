package com.projetos.agendamento.profissional.controller;

import com.projetos.agendamento.profissional.dto.DisponibilidadeRequest;
import com.projetos.agendamento.profissional.dto.DisponibilidadeResponse;
import com.projetos.agendamento.profissional.service.DisponibilidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais/{profissionalId}/disponibilidade")
@RequiredArgsConstructor
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadeResponse cadastrar(@PathVariable Long profissionalId, @Valid @RequestBody DisponibilidadeRequest request){
        return disponibilidadeService.cadastrar(profissionalId, request);
    }

    @GetMapping
    public List<DisponibilidadeResponse> listar(@PathVariable Long profissionalId){
        return disponibilidadeService.listarPorProfissional(profissionalId);
    }
}
