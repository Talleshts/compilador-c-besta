package com.ufes.compiladores.models;

import java.util.ArrayList;
import java.util.List;

public class AnalisadorLexico {
    public static void main(String[] args) {
        String code = """
            int main() {
                int x = 10;
                x = x + 1;
                //teste
                /*
                aaa
                aaa
                */
                char[5]="aaaaa";
                return x;
            }
            """;
//tava com problema no postman, depois eu apago isso
        List<Token> tokens = analyze(code);
        tokens.forEach(System.out::println);
    }

    public static List<Token> analyze(String input) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder lexeme = new StringBuilder();
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;
        boolean inString = false;
        int line = 1, column = 1;
        int lexemeStartColumn = 1;

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            char next = (i + 1 < input.length()) ? input.charAt(i + 1) : '\0';

            // Handle line and column tracking
            if (current == '\n') {
                line++;
                column = 0;
            }

            // Handle single-line comments
            if (inSingleLineComment) {
                if (current == '\n') {
                    inSingleLineComment = false;
                }
                column++;
                continue;
            }

            // Handle multi-line comments
            if (inMultiLineComment) {
                if (current == '*' && next == '/') {
                    inMultiLineComment = false;
                    i++; // Skip the '/'
                }
                column++;
                continue;
            }

            // Handle strings
            if (inString) {
                lexeme.append(current);
                if (current == '"') {
                    tokens.add(new Token(lexeme.toString(),"CADEIA DE PALAVRAS", line, lexemeStartColumn,lexeme.length(),false));
                    lexeme.setLength(0);
                    inString = false;
                }
                column++;
                continue;
            }

            // Detect start of single-line comment
            if (current == '/' && next == '/') {
                inSingleLineComment = true;
                i++;
                column += 2;
                continue;
            }

            // Detect start of multi-line comment
            if (current == '/' && next == '*') {
                inMultiLineComment = true;
                i++;
                column += 2;
                continue;
            }

            // Detect start of a string
            if (current == '"') {
                inString = true;
                lexeme.append(current);
                lexemeStartColumn = column;
                column++;
                continue;
            }

            // Check for delimiters
            if (Character.isWhitespace(current) || isDelimiter(current)) {
                if (lexeme.length() > 0) {
                    String type = identifyTokenType(lexeme.toString());
                    if (type.equals("DESCONHECIDO")) {
                        System.err.printf("Error: Unknown token '%s' at line %d, column %d%n", lexeme, line, lexemeStartColumn);
                    } else {
                        tokens.add(new Token(lexeme.toString(),type, line, lexemeStartColumn,lexeme.length(),false));
                    }
                    lexeme.setLength(0);
                }
                if (isDelimiter(current)) {
                    tokens.add(new Token(String.valueOf(current),"LIMITADOR", line, column,lexeme.length(),false));
                }
                column++;
                continue;
            }

            // Accumulate characters for the current lexeme
            if (lexeme.length() == 0) {
                lexemeStartColumn = column;
            }
            lexeme.append(current);
            column++;
        }

        // Handle any remaining lexeme
        if (lexeme.length() > 0) {
            String type = identifyTokenType(lexeme.toString());
            if (type.equals("DESCONHECIDO")) {
                System.err.printf("Error: Unknown token '%s' at line %d, column %d%n", lexeme, line, lexemeStartColumn);
            } else {
                tokens.add(new Token(lexeme.toString(), type, line, lexemeStartColumn, lexeme.length(),false));
            }
        }

        return tokens;
    }

    private static boolean isDelimiter(char c) {
        return c == ';' || c == ',' || c == '(' || c == ')' || c == '{' || c == '}' || c == '=' || c == '[' || c == ']';
    }

    private static String identifyTokenType(String lexeme) {
        // Simplified token classification
        if (lexeme.matches("\\d+")) {
            return "NUMBER";
        } else if (lexeme.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            if (lexeme.equals("int") || lexeme.equals("return") || lexeme.equals("char")) {
                return "PALAVRA RESERVADA";
            }
            return "IDENTIFICADOR";
        } else if (lexeme.matches("\\[\\d+\\]")) {
            return "ARRAY_DECLARATION";
        }
        return "UNKNOWN";
    }
}
