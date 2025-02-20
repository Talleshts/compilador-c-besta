package com.ufes.compiladores.models;

public class Simbolo {
    private String nome;
    private String tipo;
    private String escopo;
    private boolean isFunction;
    private String[] parametros;
    private boolean isArray;
    private int arraySize;
    private boolean isInitialized;
    private int linha;
    private int coluna;

    public Simbolo(String nome, String tipo, String escopo, boolean isFunction, String[] parametros,
            boolean isArray, int arraySize, boolean isInitialized, int linha, int coluna) {
        this.nome = nome;
        this.tipo = tipo;
        this.escopo = escopo;
        this.isFunction = isFunction;
        this.parametros = parametros;
        this.isArray = isArray;
        this.arraySize = arraySize;
        this.isInitialized = isInitialized;
        this.linha = linha;
        this.coluna = coluna;
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEscopo() {
        return escopo;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public String[] getParametros() {
        return parametros;
    }

    public boolean isArray() {
        return isArray;
    }

    public int getArraySize() {
        return arraySize;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    // Setters
    public void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }
}