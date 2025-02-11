package com.ufes.compiladores.models;

import java.util.HashSet;
import java.util.Set;

public class Delimitadores {
	public static final Set<String> DELIMITADORES = new HashSet<>(Set.of(
			"(", ")", "{", "}", "[", "]", ";", ",", "#"));
}