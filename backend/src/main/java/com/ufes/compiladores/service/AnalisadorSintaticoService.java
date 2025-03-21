package com.ufes.compiladores.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufes.compiladores.exceptions.SyntaxException;
import com.ufes.compiladores.models.ErroSintatico;
import com.ufes.compiladores.models.Token;

@Service
public class AnalisadorSintaticoService {
	@Autowired
	private AnalizadorLexicoService lexicalAnalyzer;

	private List<Token> tokens;
	private int currentTokenIndex;

	public List<ErroSintatico> analisar(String code) {
		List<ErroSintatico> errors = new ArrayList<>();
		tokens = lexicalAnalyzer.analisar(code);
		currentTokenIndex = 0;

		while (currentTokenIndex < tokens.size()) {
			try {
				programa();
			} catch (SyntaxException e) {
				errors.add(new ErroSintatico(
					e.getMessage(),
					tokens.get(currentTokenIndex).getLinha(),
					tokens.get(currentTokenIndex).getColuna(),
					e.getSuggestion()));

				// Tenta recuperar do erro avançando até encontrar um ponto e vírgula ou chave
				recuperarErro();
			}

			// Se chegamos ao fim dos tokens, saímos do loop
			if (currentTokenIndex >= tokens.size()) {
				break;
			}
		}

		if (errors.isEmpty()) {
			errors.add(new ErroSintatico("Parabéns! Nenhum erro de sintaxe encontrado.", 0, 0, ""));
		}

		return errors;
	}

	private void recuperarErro() {
		// Avança tokens até encontrar um ponto de sincronização (; ou })
		while (currentTokenIndex < tokens.size()) {
			Token token = tokens.get(currentTokenIndex);
			if (token.getLexema().equals(";") || token.getLexema().equals("}")) {
				currentTokenIndex++;
				break;
			}
			currentTokenIndex++;
		}
	}

	private void programa() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);

		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("#")) {
			match("#");
			match("define");
			matchTipo("ID");
			// Verifica se é um literal inteiro ou float
			Token nextToken = tokens.get(currentTokenIndex);
			if (nextToken.getTipo().equals("INT_LITERAL") || nextToken.getTipo().equals("FLOAT_LITERAL")) {
				currentTokenIndex++;
				programa();
			} else {
				throw new SyntaxException(
					"Valor inválido para #define: " + nextToken.getLexema(),
					"Use um número inteiro ou decimal (por exemplo: 100 ou 3.14)");
			}
		} else if (isDeclaracao(token)) {
			especificador();
			tipo();
			matchTipo("ID");
			programa2();
		} else {
			throw new SyntaxException(
				"Token inesperado: " + token.getLexema(), 
				"Esperava uma declaração de variável ou função.");
		}
	}

	private boolean isDeclaracao(Token token) {
		// Verifica se é uma palavra reservada de tipo ou especificador
		if (token.getTipo().equals("PALAVRA_RESERVADA")) {
			String lexema = token.getLexema();
			return lexema.equals("int") || lexema.equals("float") || 
				   lexema.equals("char") || lexema.equals("void") ||
				   lexema.equals("double") || lexema.equals("short") ||
				   lexema.equals("long") || lexema.equals("signed") ||
				   lexema.equals("unsigned") || lexema.equals("const") ||
				   lexema.equals("static") || lexema.equals("extern") ||
				   lexema.equals("auto");
		}
		// Verifica se é um identificador seguido de outro identificador (caso de redeclaração de tipo)
		return token.getTipo().equals("ID") && 
			   currentTokenIndex + 1 < tokens.size() && 
			   tokens.get(currentTokenIndex + 1).getTipo().equals("ID");
	}

	private void especificador() {
		if (currentTokenIndex >= tokens.size()) return;

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") && 
			(token.getLexema().equals("auto") || token.getLexema().equals("static") ||
			 token.getLexema().equals("extern") || token.getLexema().equals("const"))) {
			currentTokenIndex++;
		}
	}

	private void tipo() {
		if (currentTokenIndex >= tokens.size()) {
			throw new SyntaxException("Tipo esperado", "Especifique um tipo válido (int, float, char, etc).");
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") && 
			(token.getLexema().equals("void") || token.getLexema().equals("char") ||
			 token.getLexema().equals("float") || token.getLexema().equals("double") ||
			 token.getLexema().equals("int"))) {
			currentTokenIndex++;
		} else if (token.getLexema().equals("signed") || token.getLexema().equals("unsigned")) {
			currentTokenIndex++;
			inteiro();
		} else {
			inteiro();
		}
	}

	private void inteiro() {
		if (currentTokenIndex >= tokens.size()) {
			throw new SyntaxException("Tipo inteiro esperado", 
				"Use 'short', 'int' ou 'long'.");
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") && 
			(token.getLexema().equals("short") || token.getLexema().equals("int") ||
			 token.getLexema().equals("long"))) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException("Tipo inteiro inválido: " + token.getLexema(), 
				"Use 'short', 'int' ou 'long'.");
		}
	}

	private void programa2() {
		if (currentTokenIndex >= tokens.size()) return;

		Token token = tokens.get(currentTokenIndex);
		
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals(";")) {
			match(";");
			programa();
		} else if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("[")) {
			match("[");
			matchTipo("INT_LITERAL");
			match("]");
			match(";");
			programa();
		} else if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("(")) {
			match("(");
			listaParametros();
			match(")");
			bloco();
			programa();
		} else if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals(",")) {
			match(",");
			listaID();
			programa();
		} else {
			throw new SyntaxException("Token inesperado: " + token.getLexema(), 
				"Esperava ';', '[', '(' ou ','.");
		}
	}

	private void listaParametros() {
		if (currentTokenIndex >= tokens.size() || 
			tokens.get(currentTokenIndex).getTipo().equals("DELIMITADOR") && 
			tokens.get(currentTokenIndex).getLexema().equals(")")) {
			return; // Lista vazia
		}

		declaracaoParam();
		while (currentTokenIndex < tokens.size() && 
			   tokens.get(currentTokenIndex).getLexema().equals(",")) {
			match(",");
			declaracaoParam();
		}
	}

	private void declaracaoParam() {
		tipo();
		matchTipo("ID");
		if (currentTokenIndex < tokens.size() && 
			tokens.get(currentTokenIndex).getLexema().equals("[")) {
			match("[");
			// Verifica se tem um número, mas não exige
			if (currentTokenIndex < tokens.size() &&
					tokens.get(currentTokenIndex).getTipo().equals("INT_LITERAL")) {
				currentTokenIndex++;
			}
			match("]");
		}
	}

	private void bloco() {
		match("{");
		while (currentTokenIndex < tokens.size() && 
			   !tokens.get(currentTokenIndex).getLexema().equals("}")) {
			declaracaoOuInstrucao();
		}
		match("}");
	}

	private void declaracaoOuInstrucao() {
		if (currentTokenIndex >= tokens.size())
			return;

		Token token = tokens.get(currentTokenIndex);

		// Verifica se é uma declaração
		if (isDeclaracao(token)) {
			declaracaoVariavel();
		}
		// Se não é declaração, é uma instrução
		else {
			instrucao();
		}
	}

	private void declaracaoVariavel() {
		// Lê especificador se houver (static, extern, const)
		especificador();

		// Lê o tipo
		tipo();

		// Lê o primeiro identificador
		matchTipo("ID");

		// Verifica se é um array
		if (currentTokenIndex < tokens.size() &&
				tokens.get(currentTokenIndex).getLexema().equals("[")) {
			match("[");
			// Pode ser um ID ou INT_LITERAL
			Token token = tokens.get(currentTokenIndex);
			if (token.getTipo().equals("ID") || token.getTipo().equals("INT_LITERAL")) {
				currentTokenIndex++;
			} else {
				throw new SyntaxException(
						"Índice de array inválido: " + token.getLexema(),
						"Use um identificador ou número inteiro como índice.");
			}
			match("]");

			// Verifica se tem inicialização de array
			if (currentTokenIndex < tokens.size() &&
					tokens.get(currentTokenIndex).getLexema().equals("=")) {
				match("=");
				match("{");
				// Lista de valores do array
				if (!tokens.get(currentTokenIndex).getLexema().equals("}")) {
					expr();
					while (currentTokenIndex < tokens.size() &&
							tokens.get(currentTokenIndex).getLexema().equals(",")) {
						match(",");
						expr();
					}
				}
				match("}");
			}
		}
		// Verifica se tem inicialização simples
		else if (currentTokenIndex < tokens.size() &&
				tokens.get(currentTokenIndex).getLexema().equals("=")) {
			match("=");
			expr();
		}

		// Verifica se tem mais variáveis na mesma declaração
		while (currentTokenIndex < tokens.size() &&
				tokens.get(currentTokenIndex).getLexema().equals(",")) {
			match(",");
			matchTipo("ID");
			if (currentTokenIndex < tokens.size() &&
					tokens.get(currentTokenIndex).getLexema().equals("=")) {
				match("=");
				expr();
			}
		}

		match(";");
	}

	private void instrucao() {
		if (currentTokenIndex >= tokens.size()) {
			throw new SyntaxException("Fim inesperado do arquivo", 
				"Esperava uma instrução.");
		}

		Token token = tokens.get(currentTokenIndex);
		
		if (token.getTipo().equals("ID")) {
			matchTipo("ID");
			// Pode ser uma chamada de função ou atribuição
			if (currentTokenIndex < tokens.size()) {
				Token nextToken = tokens.get(currentTokenIndex);
				if (nextToken.getLexema().equals("(")) {
					// Chamada de função
					match("(");
					if (!tokens.get(currentTokenIndex).getLexema().equals(")")) {
						exprList();
					}
					match(")");
					match(";");
				} else if (nextToken.getLexema().equals("[")) {
					// Acesso a array
					match("[");
					expr();
					match("]");
					if (currentTokenIndex < tokens.size() &&
							tokens.get(currentTokenIndex).getTipo().equals("OPERADOR")) {
						atribuicao();
					}
					match(";");
				} else {
					// Atribuição simples
					atribuicao();
					match(";");
				}
			}
		}
		else if (token.getTipo().equals("PALAVRA_RESERVADA")) {
			switch (token.getLexema()) {
				case "if":
					match("if");
					match("(");
					expr();
					match(")");
					if (tokens.get(currentTokenIndex).getLexema().equals("{")) {
						bloco();
					} else {
						instrucao();
					}
					if (currentTokenIndex < tokens.size() &&
							tokens.get(currentTokenIndex).getLexema().equals("else")) {
						match("else");
						if (tokens.get(currentTokenIndex).getLexema().equals("{")) {
							bloco();
						} else {
							instrucao();
						}
					}
					break;
				case "for":
					match("for");
					match("(");
					// Inicialização
					if (!tokens.get(currentTokenIndex).getLexema().equals(";")) {
						if (isDeclaracao(tokens.get(currentTokenIndex))) {
							declaracaoVariavel();
						} else {
							expr();
							match(";");
						}
					} else {
						match(";");
					}
					// Condição
					if (!tokens.get(currentTokenIndex).getLexema().equals(";")) {
						expr();
					}
					match(";");
					// Incremento
					if (!tokens.get(currentTokenIndex).getLexema().equals(")")) {
						expr();
					}
					match(")");
					if (tokens.get(currentTokenIndex).getLexema().equals("{")) {
						bloco();
					} else {
						instrucao();
					}
					break;
				case "while":
					match("while");
					match("(");
					expr(); // A condição do while deve ser uma expressão válida
					match(")");
					if (tokens.get(currentTokenIndex).getLexema().equals("{")) {
						bloco();
					} else {
						instrucao();
					}
					break;
				case "return":
					match("return");
					if (!tokens.get(currentTokenIndex).getLexema().equals(";")) {
						expr();
					}
					match(";");
					break;
				case "printf":
					match("printf");
					match("(");
					expr();
					match(")");
					match(";");
					break;
				case "scanf":
					match("scanf");
					match("(");
					matchTipo("ID");
					match(")");
					match(";");
					break;
				case "break":
					match("break");
					match(";");
					break;
				default:
					throw new SyntaxException("Instrução inválida: " + token.getLexema(), 
							"Esperava uma instrução válida (if, for, return, printf, scanf, break).");
			}
		}
		else {
			throw new SyntaxException("Instrução inválida", 
					"Esperava uma instrução válida.");
		}
	}

	private void expressao() {
		exprRelacional();
		while (currentTokenIndex < tokens.size()) {
			Token token = tokens.get(currentTokenIndex);
			if (token.getTipo().equals("OPERADOR") &&
					(token.getLexema().equals("&&") || token.getLexema().equals("||"))) {
				currentTokenIndex++;
				exprRelacional();
			} else {
				break;
			}
		}
	}

	private void atribuicao() {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") && 
			(token.getLexema().equals("=") || token.getLexema().equals("+=") || 
			 token.getLexema().equals("-=") || token.getLexema().equals("*=") || 
						token.getLexema().equals("/=") || token.getLexema().equals("%="))) {
			currentTokenIndex++;
			exprRelacional();
		} else {
			throw new SyntaxException("Operador de atribuição inválido: " + token.getLexema(), 
					"Use '=', '+=', '-=', '*=', '/=' ou '%='.");
		}
	}

	private void expr() {
		exprRelacional();
		while (currentTokenIndex < tokens.size()) {
			Token token = tokens.get(currentTokenIndex);
			if (token.getTipo().equals("OPERADOR") && 
				(token.getLexema().equals("&&") || token.getLexema().equals("||"))) {
				currentTokenIndex++;
				exprRelacional();
			} else {
				break;
			}
		}
	}

	private void exprRelacional() {
		exprAritmetica();
		if (currentTokenIndex < tokens.size()) {
			Token token = tokens.get(currentTokenIndex);
			if (token.getTipo().equals("OPERADOR") && 
				(token.getLexema().equals(">") || token.getLexema().equals("<") ||
				 token.getLexema().equals(">=") || token.getLexema().equals("<=") ||
				 token.getLexema().equals("==") || token.getLexema().equals("!="))) {
				currentTokenIndex++;
				exprAritmetica();
			}
		}
	}

	private void exprAritmetica() {
		termo();
		while (currentTokenIndex < tokens.size()) {
			Token token = tokens.get(currentTokenIndex);
			if (token.getTipo().equals("OPERADOR") && 
				(token.getLexema().equals("+") || token.getLexema().equals("-"))) {
				currentTokenIndex++;
				termo();
			} else {
				break;
			}
		}
	}

	private void termo() {
		fator();
		while (currentTokenIndex < tokens.size()) {
			Token token = tokens.get(currentTokenIndex);
			if (token.getTipo().equals("OPERADOR") && 
				(token.getLexema().equals("*") || token.getLexema().equals("/"))) {
				currentTokenIndex++;
				fator();
			} else {
				break;
			}
		}
	}

	private void fator() {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("INT_LITERAL") || 
				token.getTipo().equals("FLOAT_LITERAL") ||
				token.getTipo().equals("STRING_LITERAL")) {
			currentTokenIndex++;
		}
		else if (token.getTipo().equals("ID")) {
			matchTipo("ID");
			if (currentTokenIndex < tokens.size()) {
				Token nextToken = tokens.get(currentTokenIndex);
				if (nextToken.getLexema().equals("(")) {
					match("(");
					exprList();
					match(")");
				} else if (nextToken.getLexema().equals("[")) {
					match("[");
					expr();
					match("]");
				} else if (nextToken.getTipo().equals("OPERADOR") &&
						(nextToken.getLexema().equals("++") || nextToken.getLexema().equals("--"))) {
					currentTokenIndex++;
				}
			}
		}
		else if (token.getLexema().equals("(")) {
			match("(");
			expr();
			match(")");
		}
		else {
			throw new SyntaxException(
					"Fator inválido: " + token.getLexema(),
					"Esperava um número, string, identificador ou expressão entre parênteses.");
		}
	}

	private void exprList() {
		if (currentTokenIndex >= tokens.size() || 
			tokens.get(currentTokenIndex).getLexema().equals(")")) {
			return; // Lista vazia
		}

		expr();
		while (currentTokenIndex < tokens.size() && 
			   tokens.get(currentTokenIndex).getLexema().equals(",")) {
			match(",");
			expr();
		}
	}

	private void match(String expectedLexeme) {
		if (currentTokenIndex >= tokens.size()) {
			throw new SyntaxException("Fim inesperado do arquivo", 
				"Esperava '" + expectedLexeme + "'.");
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getLexema().equals(expectedLexeme)) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException(
				"Esperava '" + expectedLexeme + "', encontrou '" + token.getLexema() + "'",
				"Verifique a sintaxe do programa.");
		}
	}

	private void matchTipo(String expectedType) {
		if (currentTokenIndex >= tokens.size()) {
			throw new SyntaxException("Fim inesperado do arquivo", 
				"Esperava token do tipo " + expectedType + ".");
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals(expectedType)) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException(
				"Esperava token do tipo " + expectedType + ", encontrou " + token.getTipo(),
				"Verifique a sintaxe do programa.");
		}
	}

	private void listaID() {
		matchTipo("ID");
		if (currentTokenIndex < tokens.size() && 
			tokens.get(currentTokenIndex).getLexema().equals("[")) {
			match("[");
			match("]");
		}
		if (currentTokenIndex < tokens.size() && 
			tokens.get(currentTokenIndex).getLexema().equals(",")) {
			match(",");
			listaID();
		}
	}
}