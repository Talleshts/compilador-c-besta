package com.ufes.compiladores.dto;

import org.springframework.stereotype.Component;

import com.ufes.compiladores.models.Token;

public class ResponseDTO {
    private Token token;
    private Integer errors;

    public ResponseDTO(Token token, Integer errors) {
        this.token = token;
        this.errors = errors;
    }

    public Token getToken() {
        return token;
    }

    public Integer getErrors() {
        return errors;
    }

}
