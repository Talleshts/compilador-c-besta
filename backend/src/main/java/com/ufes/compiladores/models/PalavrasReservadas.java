package com.ufes.compiladores.models;

import java.util.HashSet;
import java.util.Set;

public class PalavrasReservadas {
	public static final Set<String> PALAVRAS_RESERVADAS = new HashSet<>(Set.of(
			"auto", "static", "extern", "const", "void", "char", "float", "double",
			"signed", "unsigned", "short", "int", "long", "return", "if", "else",
			"printf", "scanf", "break", "while", "for"));
}