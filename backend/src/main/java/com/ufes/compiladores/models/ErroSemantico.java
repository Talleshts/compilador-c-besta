package com.ufes.compiladores.models;

public class ErroSemantico {
    private String mensagem;
    private int linha;
    private int coluna;
    private String sugestao;
    private String tipo;

    public ErroSemantico(String mensagem, int linha, int coluna, String sugestao, String tipo) {
        this.mensagem = mensagem;
        this.linha = linha;
        this.coluna = coluna;
        this.sugestao = sugestao;
        this.tipo = tipo;
    }

    // Getters
    public String getMensagem() {
        return mensagem;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public String getSugestao() {
        return sugestao;
    }

    public String getTipo() {
        return tipo;
    }
}