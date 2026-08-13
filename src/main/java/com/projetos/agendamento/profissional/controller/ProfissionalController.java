package com.projetos.agendamento.profissional.controller;

import com.projetos.agendamento.profissional.dto.ProfissionalRequest;
import com.projetos.agendamento.profissional.dto.ProfissionalResponse;
import com.projetos.agendamento.profissional.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfissionalResponse criar(@Valid @RequestBody ProfissionalRequest request) {
        return profissionalService.criar(request);
    }

    @GetMapping("/{id}")
    public ProfissionalResponse buscarPorId(@PathVariable Long id) {
        return profissionalService.buscarPorId(id);
    }

    @GetMapping
    public List<ProfissionalResponse> buscarTodos() {
        return profissionalService.buscarTodos();
    }

    @PutMapping("/{id}")
    public ProfissionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProfissionalRequest request) {
        return profissionalService.atualizar(id,request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        profissionalService.delete(id);
    }
}
