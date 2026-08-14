package com.projetos.agendamento.paciente.controller;
import com.projetos.agendamento.paciente.dto.PacienteRequest;
import com.projetos.agendamento.paciente.dto.PacienteResponse;
import com.projetos.agendamento.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponse criar(@Valid @RequestBody PacienteRequest request){
        return pacienteService.criar(request);
    }

    @GetMapping("/{id}")
    public PacienteResponse buscarPorId(@PathVariable Long id) {
        return pacienteService.buscarPorId(id);
    }

    @GetMapping
    public List<PacienteResponse> listarTodos() {
        return pacienteService.listarTodos();
    }

    @PutMapping("/{id}")
    public PacienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody PacienteRequest request) {
        return pacienteService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id){
        pacienteService.deletar(id);
    }
}