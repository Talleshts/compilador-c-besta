package com.ufes.compiladores.models;

public class Token {

    private String palavra;
    private String tipo;
    private Integer linha;
    private Integer coluna;
    private Integer tamanho;
    private boolean erro;

    public Token(String palavra, String tipo, Integer linha, Integer coluna, Integer tamanho, boolean erro) {
        this.palavra = palavra;
        this.tipo = tipo;
        this.linha = linha;
        this.coluna = coluna;
        this.tamanho = tamanho;
        this.erro = erro;
    }

    public String getPalavra() {
        return palavra;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getLinha() {
        return linha;
    }

    public Integer getColuna() {
        return coluna;
    }

    public Integer getTamanho() {
        return tamanho;
    }

    public boolean isErro() {
        return erro;
    }

    @Override
    public String toString() {
        return String.format("Token[type=%s, value=%s, line=%d, column=%d]", tipo, palavra, linha, coluna, tamanho);
    }
}
