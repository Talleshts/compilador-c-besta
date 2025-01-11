package com.ufes.compiladores.dto;

public record ResponseDTO(
    String message,
    Integer line,
    Integer column
) {}
