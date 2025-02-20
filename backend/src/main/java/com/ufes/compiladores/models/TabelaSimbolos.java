package com.ufes.compiladores.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabelaSimbolos {
    private Map<String, List<Simbolo>> tabela;
    private String escopoAtual;

    public TabelaSimbolos() {
        this.tabela = new HashMap<>();
        this.escopoAtual = "global";
    }

    public void inserir(Simbolo simbolo) {
        String chave = simbolo.getNome() + "@" + simbolo.getEscopo();
        tabela.computeIfAbsent(chave, k -> new ArrayList<>()).add(simbolo);
    }

    public Simbolo buscar(String nome, String escopo) {
        // Primeiro procura no escopo atual
        String chave = nome + "@" + escopo;
        List<Simbolo> simbolos = tabela.get(chave);
        if (simbolos != null && !simbolos.isEmpty()) {
            return simbolos.get(simbolos.size() - 1);
        }

        // Se não encontrar no escopo atual e não for o escopo global, irá procurar no
        // escopo global
        if (!escopo.equals("global")) {
            chave = nome + "@global";
            simbolos = tabela.get(chave);
            if (simbolos != null && !simbolos.isEmpty()) {
                return simbolos.get(simbolos.size() - 1);
            }
        }

        return null;
    }

    public void setEscopoAtual(String escopo) {
        this.escopoAtual = escopo;
    }

    public String getEscopoAtual() {
        return escopoAtual;
    }

    public List<Simbolo> getTodosSimbolos() {
        List<Simbolo> todosSimbolos = new ArrayList<>();
        for (List<Simbolo> simbolos : tabela.values()) {
            todosSimbolos.addAll(simbolos);
        }
        return todosSimbolos;
    }
}