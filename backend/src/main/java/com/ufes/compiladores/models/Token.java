package com.ufes.compiladores.models;

public class Token {

    private String tipo;
	private String lexema;
	private int linha;
	private int coluna;

	public Token(String tipo, String lexema, int linha, int coluna) {
        this.tipo = tipo;
		this.lexema = lexema;
        this.linha = linha;
		this.coluna = coluna;
    }

    public String getTipo() {
		return tipo;
    }

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getLexema() {
		return lexema;
	}

	public void setLexema(String lexema) {
		this.lexema = lexema;
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
		return String.format("Token[type=%s, value=%s, line=%d, column=%d]", tipo, lexema, linha, coluna);
    }
}
