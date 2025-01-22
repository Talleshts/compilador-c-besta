package com.ufes.compiladores.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufes.compiladores.dto.ResponseDTO;
import com.ufes.compiladores.models.Token;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/compiller")
public class CompillerController {

	private static final List<String> ESPECIFICADORES = List.of("AUTO", "STATIC", "EXTERN", "CONST");
	private static final List<String> TIPOS = List.of("VOID", "CHAR", "FLOAT", "DOUBLE", "SIGNED", "UNSIGNED");
	private static final List<String> INTEIRO = List.of("SHORT", "INT", "LONG");
	private static final String DEFINE = "#define";
	private static final List<String> RESERVADAS = List.of("if", "else", "while", "do", "for", "break", "continue",
			"return",
			"main", "define");
	private static final List<Character> OPERADORES = List.of(';', ',', '[', ']', '(', ')', '{', '}');
	private static final List<String> CARACTERES_CONEXAO_PROIBIDOS = List.of("!", "@", "#", "$", "%", "^", "&", "*",
			"-", "+",
			"=");

	private static List<String> identificadores = new ArrayList<>();

	private static boolean programaIniciado = false;

	private static int index = 0;

	@PostMapping()
	public ResponseEntity<List<Token>> postMethodName(@RequestBody String entity) {
		String[] palavras = entity.split("[\\s\\n]+");
		int linha = 1;
		int coluna = 1;

		List<Token> tokensCompilados = new ArrayList<>();
		List<ResponseDTO> response = new ArrayList<>();

		int start = 0;

		boolean esperandoIdentificador = false;

		while (index < entity.length()) {
			System.out.println("index: " + index);
			System.out.println("entity: " + entity.length());
			char caractereAtual = entity.charAt(index);

			if (Character.isWhitespace(caractereAtual)) {
				index++;
				continue;
			}

			// <programa>
			if (!programaIniciado) { // esperando cabeçalho

				// #define IDENTIFICADOR NUM <CRLF> <programa>
				if (caractereAtual == '#') { // início do define

					if (entity.startsWith(DEFINE, index)) {
						tokensCompilados.add(new Token(DEFINE, DEFINE, linha, coluna, DEFINE.length(), false));
						index += DEFINE.length();
					} else {
						start = index;
						index++; // só aqui adiciona antes de buscar
						index = buscarIndexParaPalavraCompleta(entity, index);
						tokensCompilados
								.add(new Token(entity.substring(start, index), "TOKEN DESCONHECIDO", linha,
										coluna, 1, true));
					}

					esperandoIdentificador = true;

					index = pularEspacos(entity, index);
					caractereAtual = entity.charAt(index);

					if (Character.isLetter(caractereAtual) || caractereAtual == '_') {

						start = index;
						index = buscarIndexParaPalavraCompleta(entity, index);

						if (index - start > 31) {
							tokensCompilados
									.add(new Token(entity.substring(start, index), "IDENTIFICAFOR MUITO GRANDE", linha,
											coluna, 1, true));
						}

						String word = entity.substring(start, index);

						if (identificadorEhValido(word)) {
							tokensCompilados
									.add(new Token(word, "ID", linha, coluna, word.length(), false));
							identificadores.add(word);
							esperandoIdentificador = false;

							index = pularEspacos(entity, index);
							start = index;
							index = buscarIndexParaPalavraCompleta(entity, index);

							word = entity.substring(start, index);
							caractereAtual = entity.charAt(index - 1);

							if (Character.isDigit(caractereAtual)) {
								index = pularEspacos(entity, index);
								start = index;
								index = buscarIndexParaPalavraCompleta(entity, index);
								tokensCompilados.add(new Token(entity.substring(start, index), "NUM", linha,
										coluna, index - start, false));
							} else {
								tokensCompilados
										.add(new Token(word, "NUMERO ESPERADO", linha, coluna, word.length(),
												true));
							}

						} else {
							tokensCompilados
									.add(new Token(word, "IDENTIFICADOR INVALIDO", linha, coluna, word.length(),
											true));
						}
					} else {
						String word = entity.substring(start, index);
						tokensCompilados
								.add(new Token(word, "IDENTIFICADOR ESPERADO", linha, coluna, word.length(),
										true));
					}
					continue;

				}

				// <especificador> <tipo> ID <programa2>
				else if (Character.isLetter(caractereAtual)) {
					start = index;
					index = buscarIndexParaPalavraCompleta(entity, index);

					String word = entity.substring(start, index);
					caractereAtual = entity.charAt(index - 1);

					if (ESPECIFICADORES.contains(word)) {
						tokensCompilados.add(new Token(word, "ESPECIFICADOR", linha, coluna, word.length(), false));

						index = pularEspacos(entity, index);
						start = index;
						index = buscarIndexParaPalavraCompleta(entity, index);

						word = entity.substring(start, index);
						caractereAtual = entity.charAt(index - 1);

						if (Character.isLetter(caractereAtual) && (TIPOS.contains(word) || INTEIRO.contains(word))) {

							tokensCompilados.add(new Token(word, "TIPO", linha, coluna, word.length(), false));
							boolean identificadorEsperado = true;

							if (word.equals("UNSIGNED") || word.equals("SIGNED")) {
								String signedWord = word;

								index = pularEspacos(entity, index);
								start = index;
								index = buscarIndexParaPalavraCompleta(entity, index);

								word = entity.substring(start, index);
								caractereAtual = entity.charAt(index - 1);

								if (INTEIRO.contains(word)) {
									tokensCompilados.add(new Token(String.format("%s %s", signedWord, word), "TIPO",
											linha, coluna, word.length(), false));
									identificadorEsperado = true;
								} else {
									tokensCompilados.add(new Token(String.format("%s %s", signedWord, word),
											"TIPO ESPERADO", linha, coluna, word.length(), true));
									identificadorEsperado = false;
								}
							}

							index = pularEspacos(entity, index);
							start = index;
							index = buscarIndexParaPalavraCompleta(entity, index);

							word = entity.substring(start, index);
							caractereAtual = entity.charAt(index - 1);

							if (identificadorEhValido(word) && identificadorEsperado) {
								tokensCompilados
										.add(new Token(word, "ID", linha, coluna, word.length(), false));
								identificadores.add(word);
								programaIniciado = true;
								index = pularEspacos(entity, index);
								if (caractereAtual == '(') {
									index = pularEspacos(entity, index);
									start = index;
									index = buscarIndexParaPalavraCompleta(entity, index);

									tokensCompilados
											.add(new Token(word, "OPERADORES", linha, coluna, word.length(), false));
									identificadores.add(word);

									word = entity.substring(start, index);
									caractereAtual = entity.charAt(index - 1);

									if (caractereAtual == ')') {
										index = pularEspacos(entity, index);
										start = index;
										index = buscarIndexParaPalavraCompleta(entity, index);

										tokensCompilados
												.add(new Token(word, "OPERADORES", linha, coluna, word.length(),
														false));
										identificadores.add(word);

										word = entity.substring(start, index);
										caractereAtual = entity.charAt(index - 1);

										if (caractereAtual == '{') {
											index = pularEspacos(entity, index);
											start = index;
											index = buscarIndexParaPalavraCompleta(entity, index);

											tokensCompilados
													.add(new Token(word, "OPERADORES", linha, coluna, word.length(),
															false));
											identificadores.add(word);

											word = entity.substring(start, index);
											caractereAtual = entity.charAt(index - 1);

											if (caractereAtual == '}') {
												programaIniciado = true;

												index = pularEspacos(entity, index);
												start = index;
												index = buscarIndexParaPalavraCompleta(entity, index);

												tokensCompilados
														.add(new Token(word, "OPERADORES", linha, coluna, word.length(),
																false));
												identificadores.add(word);
											}
										}
									}
								} else {
									tokensCompilados
											.add(new Token(word, "OPERADOR INVÁLIDO", linha, coluna,
													word.length(), true));
								}

							} else {
								tokensCompilados
										.add(new Token(word, "IDENTIFICADOR INVALIDO", linha, coluna, word.length(),
												true));
							}

						} else {
							tokensCompilados
									.add(new Token(word, "TIPO ESPERADO", linha, coluna, word.length(), true));
						}
					} else if (TIPOS.contains(word) || INTEIRO.contains(word)) {

						tokensCompilados.add(new Token(word, "TIPO", linha, coluna, word.length(), false));
						boolean identificadorEsperado = true;

						if (word.equals("UNSIGNED") || word.equals("SIGNED")) {
							String signedWord = word;

							index = pularEspacos(entity, index);
							start = index;
							index = buscarIndexParaPalavraCompleta(entity, index);

							word = entity.substring(start, index);
							caractereAtual = entity.charAt(index - 1);

							if (INTEIRO.contains(word)) {
								tokensCompilados.add(new Token(String.format("%s %s", signedWord, word), "TIPO",
										linha, coluna, word.length(), false));
								identificadorEsperado = true;
							} else {
								tokensCompilados.add(new Token(String.format("%s %s", signedWord, word),
										"TIPO ESPERADO", linha, coluna, word.length(), true));
								identificadorEsperado = false;
							}
						}

						index = pularEspacos(entity, index);
						start = index;
						index = buscarIndexParaPalavraCompleta(entity, index);

						word = entity.substring(start, index);
						caractereAtual = entity.charAt(index - 1);

						if (identificadorEhValido(word) && identificadorEsperado) {
							tokensCompilados
									.add(new Token(word, "ID", linha, coluna, word.length(), false));
							identificadores.add(word);

							index = pularEspacos(entity, index);
							start = index;
							index = buscarIndexParaPalavraCompleta(entity, index);

							word = entity.substring(start, index);
							caractereAtual = entity.charAt(index - 1);

							// validar programa2
							if (caractereAtual == '(') {
								index = pularEspacos(entity, index);
								start = index;
								index = buscarIndexParaPalavraCompleta(entity, index);

								tokensCompilados
										.add(new Token(word, "OPERADORES", linha, coluna, word.length(), false));
								identificadores.add(word);

								word = entity.substring(start, index);
								caractereAtual = entity.charAt(index - 1);

								if (caractereAtual == ')') {
									index = pularEspacos(entity, index);
									start = index;
									index = buscarIndexParaPalavraCompleta(entity, index);

									tokensCompilados
											.add(new Token(word, "OPERADORES", linha, coluna, word.length(), false));
									identificadores.add(word);

									word = entity.substring(start, index);
									caractereAtual = entity.charAt(index - 1);

									if (caractereAtual == '{') {
										index = pularEspacos(entity, index);
										start = index;
										index = buscarIndexParaPalavraCompleta(entity, index);

										tokensCompilados
												.add(new Token(word, "OPERADORES", linha, coluna, word.length(),
														false));
										identificadores.add(word);

										word = entity.substring(start, index);
										caractereAtual = entity.charAt(index - 1);

										if (caractereAtual == '}') {
											programaIniciado = true;

											index = pularEspacos(entity, index);
											start = index;
											index = buscarIndexParaPalavraCompleta(entity, index);

											tokensCompilados
													.add(new Token(word, "OPERADORES", linha, coluna, word.length(),
															false));
											identificadores.add(word);
										}
									}
								}
							} else {
								tokensCompilados
										.add(new Token(word, "OPERADOR INVÁLIDO", linha, coluna,
												word.length(), true));
							}

						} else {
							tokensCompilados
									.add(new Token(word, "IDENTIFICADOR INVALIDO", linha, coluna, word.length(),
											true));
						}

					} else {
						tokensCompilados
								.add(new Token(word, "ESPECIFICADOR / TIPO ESPERADO", linha, coluna, word.length(),
										true));
					}
				}

				if (caractereAtual == '\n' || caractereAtual == '\r') {
					tokensCompilados.add(new Token("CRLF", "CRLF", linha, coluna, 2, false));
					index++;
					if (caractereAtual == '\r' && index < entity.length() && entity.charAt(index) == '\n') {
						index++;
					}
					continue;
				}

				continue;
			}

			if (Character.isLetter(caractereAtual) || caractereAtual == '_') {
				start = index;
				index = buscarIndexParaPalavraCompleta(entity, index);

				if (index - start > 31) {
					tokensCompilados
							.add(new Token(entity.substring(start, index), "IDENTIFICAFOR MUITO GRANDE",
									linha,
									coluna, 1, true));
					continue;
				}

				String word = entity.substring(start, index);

				if (ESPECIFICADORES.contains(word)) {
					tokensCompilados.add(new Token(word, "ESPECIFICADOR", linha, coluna,
							word.length(), false));
				} else if (TIPOS.contains(word)) {
					tokensCompilados.add(new Token(word, "TIPO", linha, coluna, word.length(),
							false));
					esperandoIdentificador = true;
				} else if (INTEIRO.contains(word)) {
					tokensCompilados.add(new Token(word, "INTEIRO", linha, coluna, word.length(),
							false));
					esperandoIdentificador = true;
				} else if (esperandoIdentificador) {
					if (!identificadores.contains(word)) {
						tokensCompilados.add(new Token(word, "ID", linha, coluna, word.length(),
								false));
						esperandoIdentificador = false;
					} else {
						tokensCompilados
								.add(new Token(word, "IDENTIFICADOR JÁ EXISTE", linha, coluna, word.length(),
										true));
					}
				}
				continue;
			}

			if (Character.isDigit(caractereAtual)) {
				start = index;
				while (index < entity.length() && Character.isDigit(entity.charAt(index))) {
					index++;
				}
				tokensCompilados
						.add(new Token(entity.substring(start, index), "NUM", linha, coluna, index -
								start, false));
				continue;
			}

			if (ehOperador(caractereAtual)) {
				tokensCompilados.add(new Token(String.valueOf(caractereAtual), "OPERADOR", linha,
						coluna, 1, false));
				index++;
				continue;
			}

			if (caractereAtual == '\n' || caractereAtual == '\r') {
				tokensCompilados.add(new Token("CRLF", "CRLF", linha, coluna, 2, false));
				index++;
				if (caractereAtual == '\r' && index < entity.length() && entity.charAt(index) == '\n') {
					index++;
				}
				continue;
			}
		}

		return ResponseEntity.ok(tokensCompilados);

	}

	private static boolean estaNaLista(String word, String[] array) {
		for (String s : array) {
			if (s.equals(word)) {
				return true;
			}
		}
		return false;
	}

	private static boolean ehOperador(char c) {
		for (char op : OPERADORES) {
			if (op == c) {
				return true;
			}
		}
		return false;
	}

	private static Integer buscarIndexParaPalavraCompleta(String entity, int index) {
		index++;
		while (index < entity.length()
				&& (Character.isLetterOrDigit(entity.charAt(index)))) {
			index++;
		}

		return index;
	}

	private static boolean identificadorEhValido(String identificador) {
		return !identificadores.contains(identificador) && !RESERVADAS.contains(identificador)
				&& !TIPOS.contains(identificador) && !ESPECIFICADORES.contains(identificador)
				&& !INTEIRO.contains(identificador);
	}

	private static int pularEspacos(String entity, int index) {
		while (index < entity.length() && Character.isWhitespace(entity.charAt(index))) {
			index++;
		}

		return index;
	}
}