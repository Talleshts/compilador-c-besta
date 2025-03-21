package com.ufes.compiladores.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.ufes.compiladores.models.Delimitadores;
import com.ufes.compiladores.models.Operadores;
import com.ufes.compiladores.models.PalavrasReservadas;
import com.ufes.compiladores.models.Token;

@Service
public class AnalizadorLexicoService {

	private static final Pattern IDENTIFICADOR_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
	private static final Pattern NUMERO_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]+)?");
	private static final Pattern COMENTARIO_LINHA_PATTERN = Pattern.compile("^//.*");
	private static final Pattern COMENTARIO_BLOCO_INICIO_PATTERN = Pattern.compile("^/\\*");
	private static final Pattern COMENTARIO_BLOCO_FIM_PATTERN = Pattern.compile("^\\*/");
	private static final Pattern LITERAL_PATTERN = Pattern.compile("^'[^']*'");
	private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("^\"[^\"]*\"");

	private static final int MAX_IDENTIFIER_LENGTH = 31;

	public List<Token> analisar(String codigo) {
		List<Token> tokens = new ArrayList<>();
		String[] linhas = codigo.split("\\n");
		boolean emComentarioBloco = false;

		for (int numLinha = 0; numLinha < linhas.length; numLinha++) {
			String linha = linhas[numLinha];
			int coluna = 0;

			while (coluna < linha.length()) {
				// Ignora espaços em branco
				if (Character.isWhitespace(linha.charAt(coluna))) {
					coluna++;
					continue;
				}

				String restoDaLinha = linha.substring(coluna);

				// Verifica se está em um comentário de bloco
				if (emComentarioBloco) {
					Matcher fimComentarioMatcher = COMENTARIO_BLOCO_FIM_PATTERN.matcher(restoDaLinha);
					if (fimComentarioMatcher.find()) {
						coluna += 2;
						emComentarioBloco = false;
					} else {
						coluna++;
					}
					continue;
				}

				// Verifica comentários
				Matcher comentarioLinhaMatcher = COMENTARIO_LINHA_PATTERN.matcher(restoDaLinha);
				if (comentarioLinhaMatcher.find()) {
					break; // Pula para próxima linha
				}

				Matcher comentarioBlocoMatcher = COMENTARIO_BLOCO_INICIO_PATTERN.matcher(restoDaLinha);
				if (comentarioBlocoMatcher.find()) {
					emComentarioBloco = true;
					coluna += 2;
					continue;
				}

				// Verifica literais de string
				Matcher stringLiteralMatcher = STRING_LITERAL_PATTERN.matcher(restoDaLinha);
				if (stringLiteralMatcher.find()) {
					String stringLiteral = stringLiteralMatcher.group();
					tokens.add(new Token("STRING_LITERAL", stringLiteral, numLinha + 1, coluna + 1));
					coluna += stringLiteral.length();
					continue;
				}

				// Verifica literais
				Matcher literalMatcher = LITERAL_PATTERN.matcher(restoDaLinha);
				if (literalMatcher.find()) {
					String literal = literalMatcher.group();
					tokens.add(new Token("LITERAL", literal, numLinha + 1, coluna + 1));
					coluna += literal.length();
					continue;
				}

				// Verifica identificadores e palavras reservadas
				Matcher idMatcher = IDENTIFICADOR_PATTERN.matcher(restoDaLinha);
				if (idMatcher.find()) {
					String id = idMatcher.group();
					// Verifica o comprimento do identificador antes de processá-lo
					if (id.length() > MAX_IDENTIFIER_LENGTH) {
						tokens.add(new Token("ERRO",
								"Identificador excede o limite de 31 caracteres: '" + id + "'",
								numLinha + 1, coluna + MAX_IDENTIFIER_LENGTH + 1));
						coluna += id.length();
					} else {
						String tipo = PalavrasReservadas.PALAVRAS_RESERVADAS.contains(id.toLowerCase())
								? "PALAVRA_RESERVADA"
								: "ID";
						tokens.add(new Token(tipo, id, numLinha + 1, coluna + 1));
						coluna += id.length();
					}
					continue;
				}

				// Verifica números
				Matcher numeroMatcher = NUMERO_PATTERN.matcher(restoDaLinha);
				if (numeroMatcher.find()) {
					String numero = numeroMatcher.group();
					String tipo = numero.contains(".") ? "FLOAT_LITERAL" : "INT_LITERAL";
					tokens.add(new Token(tipo, numero, numLinha + 1, coluna + 1));
					coluna += numero.length();
					continue;
				}

				// Verifica operadores (do maior para o menor)
				boolean operadorEncontrado = false;
				List<String> operadoresOrdenados = new ArrayList<>(Operadores.OPERADORES);
				// Ordena operadores pelo comprimento (maior para menor)
				operadoresOrdenados.sort((a, b) -> b.length() - a.length());

				for (String op : operadoresOrdenados) {
					if (restoDaLinha.startsWith(op)) {
						tokens.add(new Token("OPERADOR", op, numLinha + 1, coluna + 1));
						coluna += op.length();
						operadorEncontrado = true;
						break;
					}
				}
				if (operadorEncontrado)
					continue;

				// Verifica delimitadores
				boolean delimitadorEncontrado = false;
				for (String del : Delimitadores.DELIMITADORES) {
					if (restoDaLinha.startsWith(del)) {
						tokens.add(new Token("DELIMITADOR", del, numLinha + 1, coluna + 1));
						coluna += del.length();
						delimitadorEncontrado = true;
						break;
					}
				}
				if (delimitadorEncontrado)
					continue;

				// Caractere não reconhecido
				tokens.add(new Token("ERRO",
						"Caractere não reconhecido: '" + linha.charAt(coluna) + "'",
						numLinha + 1, coluna + 1));
				coluna++;
			}
		}

		return tokens;
	}
}