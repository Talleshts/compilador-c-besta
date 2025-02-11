package com.ufes.compiladores.models;

public class ErroSintatico {
	private final String mensagem;
	private final int linha;
	private final int coluna;
	private final String sugestao;

	public ErroSintatico(String mensagem, int linha, int coluna, String sugestao) {
		this.mensagem = mensagem;
		this.linha = linha;
		this.coluna = coluna;
		this.sugestao = sugestao;
	}

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
}