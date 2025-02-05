package com.ufes.compiladores.models;

public class ErroSintatico {
    private String mensagem;
    private int linha;
    private int coluna;


    public ErroSintatico(String mensagem, int linha, int coluna) {
        this.mensagem = mensagem;
        this.linha = linha;
        this.coluna = coluna;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public int getColuna() {
        return coluna;
    }

    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    @Override
    public String toString() {
        return String.format("ErroSintatico[mensagem=%s, linha=%d, coluna=%d]", mensagem, linha, coluna);
    }
}
