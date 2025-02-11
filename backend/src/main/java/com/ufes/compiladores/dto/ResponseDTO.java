package com.ufes.compiladores.dto;

import java.util.List;

import com.ufes.compiladores.models.ErroSintatico;
import com.ufes.compiladores.models.Token;

public class ResponseDTO {
	private List<Token> tokens;
	private List<ErroSintatico> errosSintaticos;
	private String qualificador;
	private String tipoDeErro;

	public ResponseDTO(List<Token> tokens, List<ErroSintatico> errosSintaticos, String qualificador,
			String tipoDeErro) {
		this.tokens = tokens;
		this.errosSintaticos = errosSintaticos;
		this.qualificador = qualificador;
		this.tipoDeErro = tipoDeErro;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void setTokens(List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<ErroSintatico> getErrosSintaticos() {
		return errosSintaticos;
	}

	public void setErrosSintaticos(List<ErroSintatico> errosSintaticos) {
		this.errosSintaticos = errosSintaticos;
	}

	public String getQualificador() {
		return qualificador;
	}

	public void setQualificador(String qualificador) {
		this.qualificador = qualificador;
	}

	public String getTipoDeErro() {
		return tipoDeErro;
	}

	public void setTipoDeErro(String tipoDeErro) {
		this.tipoDeErro = tipoDeErro;
	}
}