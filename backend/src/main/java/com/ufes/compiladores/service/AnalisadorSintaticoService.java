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

		try {
			programa();
		} catch (SyntaxException e) {
			errors.add(new ErroSintatico(
					e.getMessage(),
					tokens.get(currentTokenIndex).getLinha(),
					tokens.get(currentTokenIndex).getColuna(),
					e.getSuggestion()));
		}

		return errors;
	}

	private void programa() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);

		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("#")) {
			match("#");
			matchTipo("PALAVRA_RESERVADA");
			match("define");
			matchTipo("ID");
			matchTipo("INT_LITERAL");
			programa();
		} else {
			especificador();
			tipo();
			matchTipo("ID");
			programa2();
		}
	}

	private void especificador() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") &&
				(token.getLexema().equals("auto") || token.getLexema().equals("static") ||
						token.getLexema().equals("extern") || token.getLexema().equals("const"))) {
			currentTokenIndex++;
		}
	}

	private void tipo() {
		if (currentTokenIndex >= tokens.size()) {
			throw new SyntaxException("Tipo esperado, mas não encontrado.", "Verifique a declaração do tipo.");
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") &&
				(token.getLexema().equals("void") || token.getLexema().equals("char") ||
						token.getLexema().equals("float") || token.getLexema().equals("double"))) {
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
			throw new SyntaxException("Tipo inteiro esperado, mas não encontrado.",
					"Verifique a declaração do tipo inteiro.");
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") &&
				(token.getLexema().equals("short") || token.getLexema().equals("int") ||
						token.getLexema().equals("long"))) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException("Tipo inteiro inválido: " + token.getLexema(), "Use 'short', 'int' ou 'long'.");
		}
	}

	private void programa2() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

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
			throw new SyntaxException("Token inesperado: " + token.getLexema(), "Verifique a sintaxe do programa.");
		}
	}

	private void listaID() {
		matchTipo("ID");
		declaracaoParam2();
		listaIDTail();
	}

	private void listaIDTail() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals(";")) {
			match(";");
		} else if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals(",")) {
			match(",");
			listaID();
		} else {
			throw new SyntaxException("Token inesperado: " + token.getLexema(),
					"Verifique a lista de identificadores.");
		}
	}

	private void listaParametros() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") || token.getTipo().equals("ID")) {
			listaParamRestante();
		}
	}

	private void listaParamRestante() {
		declaracaoParam();
		declParamRestante();
	}

	private void declaracaoParam() {
		tipo();
		matchTipo("ID");
		declaracaoParam2();
	}

	private void declaracaoParam2() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("[")) {
			match("[");
			matchTipo("INT_LITERAL");
			match("]");
		}
	}

	private void declParamRestante() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals(",")) {
			match(",");
			listaParamRestante();
		}
	}

	private void bloco() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("{")) {
			match("{");
			conjuntoInst();
			match("}");
		} else if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals(";")) {
			match(";");
			conjuntoInst();
		} else {
			throw new SyntaxException("Token inesperado: " + token.getLexema(), "Verifique a sintaxe do bloco.");
		}
	}

	private void conjuntoInst() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") || token.getTipo().equals("ID")) {
			programa();
			conjuntoInst();
		} else if (token.getTipo().equals("ID") || token.getTipo().equals("PALAVRA_RESERVADA")) {
			instrucoes();
			conjuntoInst();
		}
	}

	private void instrucoes() {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("ID")) {
			matchTipo("ID");
			expressao();
			match(";");
		} else if (token.getTipo().equals("PALAVRA_RESERVADA") && token.getLexema().equals("return")) {
			match("return");
			expr();
			match(";");
		} else if (token.getTipo().equals("PALAVRA_RESERVADA") && token.getLexema().equals("printf")) {
			match("printf");
			match("(");
			expr();
			match(")");
			match(";");
		} else if (token.getTipo().equals("PALAVRA_RESERVADA") && token.getLexema().equals("scanf")) {
			match("scanf");
			match("(");
			matchTipo("ID");
			match(")");
			match(";");
		} else if (token.getTipo().equals("PALAVRA_RESERVADA") && token.getLexema().equals("break")) {
			match("break");
			match(";");
		} else if (token.getTipo().equals("PALAVRA_RESERVADA") && token.getLexema().equals("if")) {
			match("if");
			match("(");
			expr();
			match(")");
			instrucoes();
			instrucoesIf();
		} else {
			throw new SyntaxException("Token inesperado: " + token.getLexema(), "Verifique a sintaxe das instruções.");
		}
	}

	private void instrucoesIf() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("PALAVRA_RESERVADA") && token.getLexema().equals("else")) {
			match("else");
			instrucoes();
		}
	}

	private void expressao() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("[")) {
			match("[");
			expr();
			match("]");
			atribuicao();
		} else if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("(")) {
			match("(");
			exprList();
			match(")");
		} else {
			atribuicao();
		}
	}

	private void atribuicao() {
		operadorAtrib();
		expr();
	}

	private void operadorAtrib() {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") &&
				(token.getLexema().equals("=") || token.getLexema().equals("*=") ||
						token.getLexema().equals("/=") || token.getLexema().equals("%=") ||
						token.getLexema().equals("+=") || token.getLexema().equals("-="))) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException("Operador de atribuição esperado, mas não encontrado.",
					"Use um operador de atribuição válido.");
		}
	}

	private void expr() {
		exprAnd();
		exprOr();
	}

	private void exprList() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		expr();
		exprListTail();
	}

	private void exprListTail() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals(",")) {
			match(",");
			exprList();
		}
	}

	private void exprOr() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") && token.getLexema().equals("||")) {
			match("||");
			exprAnd();
			exprOr();
		}
	}

	private void exprAnd() {
		exprEqual();
		exprAnd2();
	}

	private void exprAnd2() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") && token.getLexema().equals("&&")) {
			match("&&");
			exprEqual();
			exprAnd2();
		}
	}

	private void exprEqual() {
		exprRelational();
		exprEqual2();
	}

	private void exprEqual2() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") &&
				(token.getLexema().equals("==") || token.getLexema().equals("!="))) {
			currentTokenIndex++;
			exprRelational();
			exprEqual2();
		}
	}

	private void exprRelational() {
		exprPlus();
		exprRelational2();
	}

	private void exprRelational2() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") &&
				(token.getLexema().equals("<") || token.getLexema().equals("<=") ||
						token.getLexema().equals(">") || token.getLexema().equals(">="))) {
			currentTokenIndex++;
			exprPlus();
			exprRelational2();
		}
	}

	private void exprPlus() {
		exprMult();
		exprPlus2();
	}

	private void exprPlus2() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") &&
				(token.getLexema().equals("+") || token.getLexema().equals("-"))) {
			currentTokenIndex++;
			exprMult();
			exprPlus2();
		}
	}

	private void exprMult() {
		exprUnary();
		exprMult2();
	}

	private void exprMult2() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") &&
				(token.getLexema().equals("*") || token.getLexema().equals("/"))) {
			currentTokenIndex++;
			exprUnary();
			exprMult2();
		}
	}

	private void exprUnary() {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("OPERADOR") &&
				(token.getLexema().equals("+") || token.getLexema().equals("-"))) {
			currentTokenIndex++;
			exprParenthesis();
		} else {
			exprParenthesis();
		}
	}

	private void exprParenthesis() {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("(")) {
			match("(");
			expr();
			match(")");
		} else {
			primary();
		}
	}

	private void primary() {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("ID")) {
			matchTipo("ID");
			primaryID();
		} else if (token.getTipo().equals("INT_LITERAL") || token.getTipo().equals("FLOAT_LITERAL")
				|| token.getTipo().equals("LITERAL")) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException("Token primário esperado, mas não encontrado.", "Verifique a expressão.");
		}
	}

	private void primaryID() {
		if (currentTokenIndex >= tokens.size()) {
			return; // ε production
		}

		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("[")) {
			match("[");
			primary();
			match("]");
		} else if (token.getTipo().equals("DELIMITADOR") && token.getLexema().equals("(")) {
			match("(");
			exprList();
			match(")");
		}
	}

	private void match(String expectedLexeme) {
		Token token = tokens.get(currentTokenIndex);
		if (token.getLexema().equals(expectedLexeme)) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException(
					"Esperado '" + expectedLexeme + "', encontrado '" + token.getLexema() + "'",
					"Verifique a sintaxe.");
		}
	}

	private void matchTipo(String expectedType) {
		Token token = tokens.get(currentTokenIndex);
		if (token.getTipo().equals(expectedType)) {
			currentTokenIndex++;
		} else {
			throw new SyntaxException(
					"Esperado token do tipo " + expectedType + ", encontrado " + token.getTipo(),
					"Verifique a sintaxe.");
		}
	}
}