package com.projetos.agendamento.utils.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException exception) {
        ApiError erro = new ApiError(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Acesso Negado",
                exception.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        ApiError erro = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Não Encontrado",
                exception.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    public ResponseEntity<ApiError> handleGeneric(Exception exception) {
        ApiError erro = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    public ResponseEntity<ApiError> handleBusinessRule(IllegalArgumentException exception){
        ApiError erro = new ApiError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Business Rule Violation",
                exception.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }
}