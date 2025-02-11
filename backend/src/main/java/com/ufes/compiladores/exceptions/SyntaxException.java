package com.ufes.compiladores.exceptions;

public class SyntaxException extends RuntimeException {
	private final String suggestion;

	public SyntaxException(String message, String suggestion) {
		super(message);
		this.suggestion = suggestion;
	}

	public String getSuggestion() {
		return suggestion;
	}
}