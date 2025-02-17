package com.ufes.compiladores.models;

import java.util.HashSet;
import java.util.Set;

public class Operadores {
	public static final Set<String> OPERADORES = new HashSet<>(Set.of(
			"+", "-", "*", "/", "=", "==", "!=", "<", "<=", ">", ">=", "+=", "-=",
			"*=", "/=", "%=", "&&", "||", "++", "--"));
}