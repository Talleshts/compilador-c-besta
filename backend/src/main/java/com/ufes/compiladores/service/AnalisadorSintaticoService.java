package com.ufes.compiladores.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufes.compiladores.models.ErroSintatico;
import com.ufes.compiladores.models.Token;

@Service
public class AnalisadorSintaticoService {
     @Autowired
    private AnalizadorLexicoService lexicalAnalyzer;
    
    private List<Token> tokens;
    private int currentTokenIndex;
    
    public List<ErroSintatico> analisar(String code) {
        List<ErroSintatico> errors = new ArrayList<>();
        tokens = lexicalAnalyzer.analisar(code);
        currentTokenIndex = 0;
        
        try {
            programa();
        } catch (SyntaxException e) {
            errors.add(new ErroSintatico(
                e.getMessage(),
                tokens.get(currentTokenIndex).getLinha(),
                tokens.get(currentTokenIndex).getColuna()
            ));
        }
        
        return errors;
    }
    
    private void programa() {
        if (currentTokenIndex >= tokens.size()) {
            return; // ε production
        }
        
        Token token = tokens.get(currentTokenIndex);
        
        if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("#")) {
            // #define ID NUM <CRLF> <programa>
            match("#");
            matchTipo("PALAVRA_RESERVADA", "define");
            matchTipo("ID");
            matchTipo("INT_LITERAL");
            // Assumindo que CRLF é tratado implicitamente
            programa();
        } else {
            // <especificador> <tipo> ID <programa2>
            especificador();
            tipo();
            matchTipo("ID");
            programa2();
        }
    }
    
    private void match(String expectedLexeme) {
        Token token = tokens.get(currentTokenIndex);
        if (token.getLexema().equals(expectedLexeme)) {
            currentTokenIndex++;
        } else {
            throw new SyntaxException(
                "Esperado '" + expectedLexeme + "', encontrado '" + token.getLexema() + "'"
            );
        }
    }
    
    private void matchTipo(String expectedType) {
        Token token = tokens.get(currentTokenIndex);
        if (token.getTipo().equals(expectedType)) {
            currentTokenIndex++;
        } else {
            throw new SyntaxException(
                "Esperado token do tipo " + expectedType + ", encontrado " + token.getTipo()
            );
        }
    }
}
