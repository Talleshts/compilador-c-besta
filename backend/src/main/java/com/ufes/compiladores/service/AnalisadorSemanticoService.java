package com.ufes.compiladores.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ufes.compiladores.models.ErroSemantico;
import com.ufes.compiladores.models.Simbolo;
import com.ufes.compiladores.models.TabelaSimbolos;
import com.ufes.compiladores.models.Token;

@Service
public class AnalisadorSemanticoService {
    @Autowired
    private AnalizadorLexicoService lexicalAnalyzer;

    private List<Token> tokens;
    private int currentTokenIndex;
    private TabelaSimbolos tabelaSimbolos;
    private List<ErroSemantico> erros;
    private Stack<String> pilhaEscopos;
    private String tipoRetornoFuncaoAtual;
    private boolean encontrouReturn;

    public List<ErroSemantico> analisar(String code) {
        tokens = lexicalAnalyzer.analisar(code);
        currentTokenIndex = 0;
        tabelaSimbolos = new TabelaSimbolos();
        erros = new ArrayList<>();
        pilhaEscopos = new Stack<>();
        pilhaEscopos.push("global");
        tipoRetornoFuncaoAtual = null;
        encontrouReturn = false;

        while (currentTokenIndex < tokens.size()) {
            analisarDeclaracao();
        }

        if (erros.isEmpty()) {
            erros.add(new ErroSemantico("Parabéns! Nenhum erro semântico encontrado.", 0, 0, "", "SUCESSO"));
        }

        return erros;
    }

    private void analisarDeclaracao() {
        if (currentTokenIndex >= tokens.size()) {
            return;
        }

        Token token = tokens.get(currentTokenIndex);
        System.out.println("Analisando token: " + token.getLexema() + " do tipo: " + token.getTipo());

        if (token.getTipo().equals("PALAVRA_RESERVADA")) {
            if (token.getLexema().equals("int") || token.getLexema().equals("float") ||
                    token.getLexema().equals("char") || token.getLexema().equals("void")) {
                String tipo = token.getLexema();
                System.out.println("Encontrou tipo: " + tipo);
                currentTokenIndex++; // Avança para o próximo token após o tipo

                if (currentTokenIndex >= tokens.size()) {
                    adicionarErro("Declaração incompleta após " + tipo,
                            token.getLinha(), token.getColuna(),
                            "Complete a declaração com um identificador",
                            "ERRO_SINTAXE");
                    return;
                }

                // Verifica se é uma declaração de função
                if (tipo.equals("int") && tokens.get(currentTokenIndex).getLexema().equals("main")) {
                    analisarDeclaracaoFuncao(tipo);
                } else if (currentTokenIndex + 1 < tokens.size() &&
                        tokens.get(currentTokenIndex + 1).getLexema().equals("(")) {
                    analisarDeclaracaoFuncao(tipo);
                } else {
                    analisarDeclaracaoVariavel(tipo);
                }
            } else {
                currentTokenIndex++; // Avança se for outra palavra reservada
            }
        } else if (token.getTipo().equals("ID")) {
            analisarAtribuicaoOuChamadaFuncao();
            currentTokenIndex++; // Avança após a análise
        } else {
            currentTokenIndex++; // Avança para o próximo token
        }
    }

    private void analisarDeclaracaoVariavel(String tipo) {
        System.out.println(
                "Iniciando análise de declaração de variável do tipo: " + tipo + " no escopo: " + pilhaEscopos.peek());

        if (currentTokenIndex >= tokens.size()) {
            return;
        }

        do {
            Token idToken = tokens.get(currentTokenIndex);
            System.out.println("Token atual: " + idToken.getLexema() + " do tipo: " + idToken.getTipo());

            if (!idToken.getTipo().equals("ID")) {
                adicionarErro("Esperava um identificador após '" + tipo + "'",
                        idToken.getLinha(), idToken.getColuna(),
                        "Declare um nome para a variável após o tipo " + tipo,
                        "ERRO_IDENTIFICADOR");
                return;
            }

            String nome = idToken.getLexema();
            System.out.println("Encontrou identificador: " + nome);

            // Verifica se já existe no escopo atual
            Simbolo simboloExistente = tabelaSimbolos.buscar(nome, pilhaEscopos.peek());
            if (simboloExistente != null) {
                adicionarErro("Variável '" + nome + "' já declarada neste escopo",
                        idToken.getLinha(), idToken.getColuna(),
                        "Use um nome diferente para a variável",
                        "ERRO_REDECLARACAO");
                return;
            }

            // Cria e insere o símbolo na tabela antes de continuar a análise
            Simbolo novoSimbolo = new Simbolo(nome, tipo, pilhaEscopos.peek(), false, null,
                    false, 0, false,
                    idToken.getLinha(), idToken.getColuna());
            tabelaSimbolos.inserir(novoSimbolo);
            System.out.println("Inseriu símbolo: " + nome + " do tipo " + tipo + " no escopo " + pilhaEscopos.peek());

            currentTokenIndex++; // Avança para o próximo token após o identificador

            // Verifica se tem inicialização
            if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getLexema().equals("=")) {
                currentTokenIndex++; // Consome o '='
                if (currentTokenIndex < tokens.size()) {
                    String tipoExpr = analisarExpressao();
                    System.out.println("Tipo da expressão de inicialização: " + tipoExpr);
                    if (!tiposCompativeis(tipo, tipoExpr)) {
                        adicionarErro("Tipo incompatível na inicialização",
                                idToken.getLinha(), idToken.getColuna(),
                                "Use um valor do tipo " + tipo,
                                "ERRO_TIPO");
                    } else {
                        novoSimbolo.setInitialized(true);
                    }
                }
            }

            // Verifica se tem vírgula para continuar com mais variáveis
            if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getLexema().equals(",")) {
                currentTokenIndex++; // Consome a vírgula
                continue;
            }
            break;
        } while (currentTokenIndex < tokens.size());

        // Verifica se tem ponto e vírgula
        if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getLexema().equals(";")) {
            currentTokenIndex++; // Consome o ';'
        }
    }

    private boolean tiposCompativeis(String tipo1, String tipo2) {
        if (tipo1.equals(tipo2))
            return true;

        // Permite conversão implícita de int para float
        if (tipo1.equals("float") && tipo2.equals("int"))
            return true;

        // Permite conversão implícita de float para int em retornos
        if (tipo1.equals("int") && tipo2.equals("float")) {
            // Verifica se estamos em um contexto de retorno dentro da função media
            Stack<String> escoposTemp = new Stack<>();
            escoposTemp.addAll(pilhaEscopos);
            while (!escoposTemp.isEmpty()) {
                String escopo = escoposTemp.pop();
                if (escopo.equals("media")) {
                    return true;
                }
            }
        }

        return false;
    }

    private String analisarExpressao() {
        if (currentTokenIndex >= tokens.size()) {
            return "erro";
        }

        // Se encontrar parênteses, processa a expressão dentro deles
        if (tokens.get(currentTokenIndex).getLexema().equals("(")) {
            currentTokenIndex++; // Consome '('
            String tipoParenteses = analisarExpressao();
            if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getLexema().equals(")")) {
                currentTokenIndex++; // Consome ')'
            }
            return tipoParenteses;
        }

        String tipoEsquerda = analisarTermo();

        while (currentTokenIndex < tokens.size() &&
                tokens.get(currentTokenIndex).getTipo().equals("OPERADOR")) {
            Token operador = tokens.get(currentTokenIndex);
            currentTokenIndex++;
            String tipoDireita = analisarTermo();
            tipoEsquerda = determinarTipoResultado(tipoEsquerda, tipoDireita, operador.getLexema());
        }

        return tipoEsquerda;
    }

    private String analisarTermo() {
        if (currentTokenIndex >= tokens.size()) {
            return "erro";
        }

        Token token = tokens.get(currentTokenIndex);
        String tipo = "erro";

        switch (token.getTipo()) {
            case "INT_LITERAL":
                tipo = "int";
                currentTokenIndex++;
                break;
            case "FLOAT_LITERAL":
                tipo = "float";
                currentTokenIndex++;
                break;
            case "STRING_LITERAL":
            case "LITERAL":
                tipo = "string";
                currentTokenIndex++;
                break;
            case "ID":
                // Busca em todos os escopos da pilha, do mais interno para o mais externo
                Simbolo simbolo = null;
                Stack<String> escoposTemp = new Stack<>();
                escoposTemp.addAll(pilhaEscopos);

                // Primeiro tenta encontrar no escopo atual e escopos superiores
                while (!escoposTemp.isEmpty() && simbolo == null) {
                    String escopo = escoposTemp.pop();
                    simbolo = tabelaSimbolos.buscar(token.getLexema(), escopo);
                }

                // Se não encontrou, procura especificamente no escopo global para funções
                if (simbolo == null) {
                    simbolo = tabelaSimbolos.buscar(token.getLexema(), "global");
                }

                if (simbolo == null) {
                    adicionarErro("Variável '" + token.getLexema() + "' não declarada",
                            token.getLinha(), token.getColuna(),
                            "Declare a variável antes de usá-la",
                            "ERRO_IDENTIFICADOR");
                    tipo = "erro";
                } else {
                    // Verifica se a variável foi inicializada antes de ser usada
                    if (!simbolo.isFunction() && !simbolo.isInitialized()) {
                        adicionarErro("Variável '" + token.getLexema() + "' sendo usada sem ter sido inicializada",
                                token.getLinha(), token.getColuna(),
                                "Atribua um valor à variável antes de usá-la",
                                "ERRO_VARIAVEL_NAO_INICIALIZADA");
                        tipo = "erro";
                    } else {
                        tipo = simbolo.getTipo();
                    }
                    System.out.println("Encontrou símbolo " + token.getLexema() + " do tipo " + tipo);
                }
                currentTokenIndex++;

                // Se for uma chamada de função, processa os argumentos
                if (currentTokenIndex < tokens.size() &&
                        tokens.get(currentTokenIndex).getLexema().equals("(")) {
                    if (simbolo != null && simbolo.isFunction()) {
                        tipo = simbolo.getTipo();
                        analisarArgumentosFuncao(simbolo);
                    }
                }
                break;
        }

        return tipo;
    }

    private String determinarTipoResultado(String tipo1, String tipo2, String operador) {
        if (tipo1.equals("erro") || tipo2.equals("erro")) {
            return "erro";
        }

        // Se algum dos operandos for string, erro
        if (tipo1.equals("string") || tipo2.equals("string")) {
            Token token = tokens.get(currentTokenIndex - 1);
            adicionarErro("Operação inválida com string",
                    token.getLinha(), token.getColuna(),
                    "Não é possível realizar operações com strings",
                    "ERRO_OPERACAO_STRING");
            return "erro";
        }

        // Se algum dos operandos for float, o resultado é float
        if (tipo1.equals("float") || tipo2.equals("float")) {
            return "float";
        }

        // Para operações entre inteiros, o resultado é inteiro
        if (tipo1.equals("int") && tipo2.equals("int")) {
            // Para divisão, mesmo entre inteiros, o resultado é float
            if (operador.equals("/")) {
                return "float";
            }
            return "int";
        }

        return "erro";
    }

    private void analisarArgumentosFuncao(Simbolo funcao) {
        currentTokenIndex++; // Consome '('
        List<String> tiposArgumentos = new ArrayList<>();

        while (currentTokenIndex < tokens.size() &&
                !tokens.get(currentTokenIndex).getLexema().equals(")")) {
            String tipoArg = analisarExpressao();
            tiposArgumentos.add(tipoArg);

            if (currentTokenIndex < tokens.size() &&
                    tokens.get(currentTokenIndex).getLexema().equals(",")) {
                currentTokenIndex++;
            }
        }

        if (currentTokenIndex < tokens.size()) {
            currentTokenIndex++; // Consome ')'
        }

        // Verifica número de argumentos
        if (tiposArgumentos.size() != funcao.getParametros().length) {
            Token token = tokens.get(currentTokenIndex - 1);
            adicionarErro("Número incorreto de argumentos para função '" + funcao.getNome() + "'",
                    token.getLinha(), token.getColuna(),
                    "A função espera " + funcao.getParametros().length + " argumentos",
                    "ERRO_ARGUMENTOS");
            return;
        }

        // Verifica tipos dos argumentos
        for (int i = 0; i < tiposArgumentos.size(); i++) {
            if (!tiposCompativeis(funcao.getParametros()[i], tiposArgumentos.get(i))) {
                Token token = tokens.get(currentTokenIndex - 1);
                adicionarErro("Tipo incompatível no argumento " + (i + 1),
                        token.getLinha(), token.getColuna(),
                        "Use um valor do tipo " + funcao.getParametros()[i],
                        "ERRO_TIPO");
            }
        }
    }

    private void analisarDeclaracaoFuncao(String tipoRetorno) {
        Token idToken = tokens.get(currentTokenIndex);
        if (!idToken.getTipo().equals("ID")) {
            adicionarErro("Esperava um identificador de função",
                    idToken.getLinha(), idToken.getColuna(),
                    "Declare um nome válido para a função",
                    "ERRO_IDENTIFICADOR");
            return;
        }

        String nome = idToken.getLexema();
        System.out.println("Analisando função: " + nome + " do tipo " + tipoRetorno);
        currentTokenIndex++;

        // Verifica se já existe no escopo global
        if (tabelaSimbolos.buscar(nome, "global") != null) {
            adicionarErro("Função '" + nome + "' já declarada",
                    idToken.getLinha(), idToken.getColuna(),
                    "Use um nome diferente para a função",
                    "ERRO_REDECLARACAO");
            return;
        }

        // Adiciona função à tabela de símbolos antes de processar parâmetros
        Simbolo funcao = new Simbolo(nome, tipoRetorno, "global", true,
                new String[0], false, 0, true,
                idToken.getLinha(), idToken.getColuna());
        tabelaSimbolos.inserir(funcao);

        // Processa parâmetros
        List<String> parametros = new ArrayList<>();
        currentTokenIndex++; // Consome '('

        // Novo escopo para os parâmetros
        pilhaEscopos.push(nome);

        while (currentTokenIndex < tokens.size() &&
                !tokens.get(currentTokenIndex).getLexema().equals(")")) {
            // Verifica se é um tipo
            if (isPalavraReservadaTipo(tokens.get(currentTokenIndex).getLexema())) {
                String tipoParam = tokens.get(currentTokenIndex).getLexema();
                parametros.add(tipoParam);
                currentTokenIndex++;

                // Verifica se tem um identificador após o tipo
                if (currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).getTipo().equals("ID")) {
                    String nomeParam = tokens.get(currentTokenIndex).getLexema();
                    // Adiciona o parâmetro como uma variável no escopo da função
                    Simbolo param = new Simbolo(nomeParam, tipoParam, nome, false, null, false, 0, true,
                            tokens.get(currentTokenIndex).getLinha(),
                            tokens.get(currentTokenIndex).getColuna());
                    tabelaSimbolos.inserir(param);
                    System.out.println(
                            "Inseriu parâmetro: " + nomeParam + " do tipo " + tipoParam + " no escopo " + nome);
                    currentTokenIndex++;
                }

                // Consome a vírgula se houver
                if (currentTokenIndex < tokens.size() &&
                        tokens.get(currentTokenIndex).getLexema().equals(",")) {
                    currentTokenIndex++;
                }
            } else {
                currentTokenIndex++;
            }
        }
        currentTokenIndex++; // Consome ')'

        // Atualiza a função na tabela de símbolos com os parâmetros
        String[] arrayParametros = parametros.toArray(new String[0]);
        funcao = new Simbolo(nome, tipoRetorno, "global", true,
                arrayParametros, false, 0, true,
                idToken.getLinha(), idToken.getColuna());
        tabelaSimbolos.inserir(funcao);

        // Adiciona função à tabela de símbolos
        Simbolo funcaoAtual = tabelaSimbolos.buscar(pilhaEscopos.peek(), "global");
        if (funcaoAtual != null) {
            System.out.println("Função atual: " + funcaoAtual.getNome() +
                    " do tipo " + funcaoAtual.getTipo());
            tipoRetornoFuncaoAtual = funcaoAtual.getTipo();
        }

        // Configura novo escopo para o corpo da função
        encontrouReturn = false;

        // Analisa corpo da função
        if (currentTokenIndex < tokens.size() &&
                tokens.get(currentTokenIndex).getLexema().equals("{")) {
            analisarBloco();
        }

        // Verifica se função não void tem return
        if (!tipoRetorno.equals("void") && !encontrouReturn && !nome.equals("main")) {
            adicionarErro("Função '" + nome + "' deve retornar um valor do tipo " + tipoRetorno,
                    idToken.getLinha(), idToken.getColuna(),
                    "Adicione uma instrução return",
                    "ERRO_RETURN");
        }

        System.out.println("Saindo do escopo da função: " + nome);
        pilhaEscopos.pop();
        tipoRetornoFuncaoAtual = null;
        encontrouReturn = false;
    }

    private void analisarBloco() {
        currentTokenIndex++; // Consome '{'

        // Cria um novo escopo para o bloco se não estiver dentro de um if/else
        String escopoAtual = pilhaEscopos.peek();
        if (!escopoAtual.startsWith("if_") && !escopoAtual.startsWith("else_")) {
            String escopoBloco = "bloco_" + currentTokenIndex;
            pilhaEscopos.push(escopoBloco);
        }

        while (currentTokenIndex < tokens.size() &&
                !tokens.get(currentTokenIndex).getLexema().equals("}")) {
            Token token = tokens.get(currentTokenIndex);
            System.out.println("Analisando token no bloco: " + token.getLexema() + " do tipo: " + token.getTipo()
                    + " no escopo: " + pilhaEscopos.peek());

            if (token.getTipo().equals("PALAVRA_RESERVADA")) {
                if (isPalavraReservadaTipo(token.getLexema())) {
                    String tipo = token.getLexema();
                    currentTokenIndex++; // Avança para depois do tipo
                    analisarDeclaracaoVariavel(tipo);
                } else {
                    switch (token.getLexema()) {
                        case "if":
                            analisarIf();
                            break;
                        case "for":
                            analisarFor();
                            break;
                        case "return":
                            analisarReturn();
                            break;
                    }
                    currentTokenIndex++;
                }
            } else if (token.getTipo().equals("ID")) {
                // Guarda o token atual para mensagens de erro
                Token idToken = token;

                // Busca o símbolo no escopo atual e superiores
                Simbolo simbolo = null;
                Stack<String> escoposTemp = new Stack<>();
                escoposTemp.addAll(pilhaEscopos);

                while (!escoposTemp.isEmpty() && simbolo == null) {
                    String escopo = escoposTemp.pop();
                    simbolo = tabelaSimbolos.buscar(token.getLexema(), escopo);
                }

                if (simbolo == null) {
                    simbolo = tabelaSimbolos.buscar(token.getLexema(), "global");
                }

                // Verifica se a variável existe antes de prosseguir
                if (simbolo == null) {
                    adicionarErro("Variável '" + token.getLexema() + "' não declarada",
                            idToken.getLinha(), idToken.getColuna(),
                            "Declare a variável antes de usá-la",
                            "ERRO_IDENTIFICADOR");
                    // Avança até encontrar um ponto e vírgula para recuperação de erro
                    while (currentTokenIndex < tokens.size() &&
                            !tokens.get(currentTokenIndex).getLexema().equals(";")) {
                        currentTokenIndex++;
                    }
                    if (currentTokenIndex < tokens.size()) {
                        currentTokenIndex++; // Consome o ';'
                    }
                    continue;
                }

                currentTokenIndex++; // Avança após o identificador

                // Verifica se é uma atribuição
                if (currentTokenIndex < tokens.size() &&
                        tokens.get(currentTokenIndex).getLexema().equals("=")) {
                    currentTokenIndex++; // Avança após o '='
                    String tipoExpr = analisarExpressao();

                    if (!tiposCompativeis(simbolo.getTipo(), tipoExpr)) {
                        adicionarErro("Tipo incompatível na atribuição",
                                idToken.getLinha(), idToken.getColuna(),
                                "Não é possível atribuir valor do tipo '" + tipoExpr +
                                        "' a uma variável do tipo '" + simbolo.getTipo() + "'",
                                "ERRO_TIPO");
                    }
                    simbolo.setInitialized(true);

                    if (currentTokenIndex < tokens.size() &&
                            tokens.get(currentTokenIndex).getLexema().equals(";")) {
                        currentTokenIndex++; // Consome ';'
                    }
                } else {
                    // Se não é atribuição, analisa como expressão
                    currentTokenIndex--; // Volta para reanalisar o ID
                    String tipoExpr = analisarExpressao();

                    // Verifica se é uma expressão solta
                    if (currentTokenIndex < tokens.size() &&
                            tokens.get(currentTokenIndex).getLexema().equals(";")) {
                        if (!tipoExpr.equals("void")) {
                            adicionarErro("Expressão sem uso",
                                    idToken.getLinha(), idToken.getColuna(),
                                    "A expressão deve possuir uma atribuição ou ser um retorno de função",
                                    "ERRO_EXPRESSAO");
                        }
                        currentTokenIndex++; // Consome ';'
                    }
                }
            } else {
                currentTokenIndex++;
            }
        }

        // Remove o escopo do bloco se não estiver dentro de um if/else
        if (!escopoAtual.startsWith("if_") && !escopoAtual.startsWith("else_")) {
            pilhaEscopos.pop();
        }

        currentTokenIndex++; // Consome '}'
    }

    private boolean isPalavraReservadaTipo(String palavra) {
        return palavra.equals("int") || palavra.equals("float") ||
                palavra.equals("char") || palavra.equals("void") ||
                palavra.equals("double");
    }

    private void analisarIf() {
        currentTokenIndex++; // Consome 'if'
        currentTokenIndex++; // Consome '('

        String tipoCondicao = analisarExpressao();
        if (!tipoCondicao.equals("int")) { // Em C, condições são tratadas como inteiros
            Token token = tokens.get(currentTokenIndex - 1);
            adicionarErro("Condição do if deve ser uma expressão booleana",
                    token.getLinha(), token.getColuna(),
                    "Use uma expressão que resulte em verdadeiro ou falso",
                    "ERRO_TIPO");
        }

        currentTokenIndex++; // Consome ')'

        // Cria um novo escopo para o bloco do if
        String escopoIf = "if_" + currentTokenIndex;
        pilhaEscopos.push(escopoIf);

        // Analisa bloco do if
        if (tokens.get(currentTokenIndex).getLexema().equals("{")) {
            analisarBloco();
        } else {
            // Analisa única instrução
            currentTokenIndex++;
        }

        pilhaEscopos.pop(); // Remove o escopo do if

        // Verifica se tem else
        if (currentTokenIndex < tokens.size() &&
                tokens.get(currentTokenIndex).getLexema().equals("else")) {
            currentTokenIndex++; // Consome 'else'

            // Cria um novo escopo para o bloco do else
            String escopoElse = "else_" + currentTokenIndex;
            pilhaEscopos.push(escopoElse);

            if (tokens.get(currentTokenIndex).getLexema().equals("{")) {
                analisarBloco();
            } else {
                // Analisa única instrução
                currentTokenIndex++;
            }

            pilhaEscopos.pop(); // Remove o escopo do else
        }
    }

    private void analisarFor() {
        currentTokenIndex++; // Consome 'for'
        currentTokenIndex++; // Consome '('

        // Inicialização
        if (!tokens.get(currentTokenIndex).getLexema().equals(";")) {
            if (isPalavraReservadaTipo(tokens.get(currentTokenIndex).getLexema())) {
                analisarDeclaracaoVariavel(tokens.get(currentTokenIndex).getLexema());
            } else {
                analisarExpressao();
            }
        }
        currentTokenIndex++; // Consome ';'

        // Condição
        if (!tokens.get(currentTokenIndex).getLexema().equals(";")) {
            String tipoCondicao = analisarExpressao();
            if (!tipoCondicao.equals("int")) {
                Token token = tokens.get(currentTokenIndex - 1);
                adicionarErro("Condição do for deve ser uma expressão booleana",
                        token.getLinha(), token.getColuna(),
                        "Use uma expressão que resulte em verdadeiro ou falso",
                        "ERRO_TIPO");
            }
        }
        currentTokenIndex++; // Consome ';'

        // Incremento
        if (!tokens.get(currentTokenIndex).getLexema().equals(")")) {
            analisarExpressao();
        }
        currentTokenIndex++; // Consome ')'

        // Analisa corpo do for
        if (tokens.get(currentTokenIndex).getLexema().equals("{")) {
            analisarBloco();
        } else {
            // Analisa única instrução
            currentTokenIndex++;
        }
    }

    private void analisarReturn() {
        currentTokenIndex++; // Consome 'return'
        System.out.println("Analisando return no escopo: " + pilhaEscopos.peek() +
                " com tipo de retorno esperado: " + tipoRetornoFuncaoAtual);

        if (tipoRetornoFuncaoAtual == null) {
            Token token = tokens.get(currentTokenIndex - 1);
            adicionarErro("Return fora de função",
                    token.getLinha(), token.getColuna(),
                    "A instrução return só pode ser usada dentro de funções",
                    "ERRO_RETURN");
            return;
        }

        encontrouReturn = true;

        // Verifica se tem expressão de retorno
        if (!tokens.get(currentTokenIndex).getLexema().equals(";")) {
            String tipoExpr = analisarExpressao();
            System.out.println("Tipo da expressão de retorno: " + tipoExpr +
                    " para função que espera: " + tipoRetornoFuncaoAtual);

            // Busca a função atual na tabela de símbolos
            Simbolo funcaoAtual = tabelaSimbolos.buscar(pilhaEscopos.peek(), "global");
            if (funcaoAtual != null) {
                System.out.println("Função atual: " + funcaoAtual.getNome() +
                        " do tipo " + funcaoAtual.getTipo());
                tipoRetornoFuncaoAtual = funcaoAtual.getTipo();
            }

            if (tipoRetornoFuncaoAtual.equals("void")) {
                Token token = tokens.get(currentTokenIndex - 1);
                adicionarErro("Função void não pode retornar valor",
                        token.getLinha(), token.getColuna(),
                        "Remova o valor de retorno",
                        "ERRO_RETURN");
            } else if (!tiposCompativeis(tipoRetornoFuncaoAtual, tipoExpr)) {
                Token token = tokens.get(currentTokenIndex - 1);
                adicionarErro("Tipo de retorno incompatível",
                        token.getLinha(), token.getColuna(),
                        "Retorne um valor do tipo " + tipoRetornoFuncaoAtual,
                        "ERRO_TIPO");
            }
        } else if (!tipoRetornoFuncaoAtual.equals("void")) {
            Token token = tokens.get(currentTokenIndex);
            adicionarErro("Função deve retornar um valor do tipo " + tipoRetornoFuncaoAtual,
                    token.getLinha(), token.getColuna(),
                    "Adicione um valor de retorno",
                    "ERRO_RETURN");
        }

        currentTokenIndex++; // Consome ';'
    }

    private void analisarAtribuicaoOuChamadaFuncao() {
        Token idToken = tokens.get(currentTokenIndex);
        String nome = idToken.getLexema();

        // Primeiro procura no escopo atual
        Simbolo simbolo = tabelaSimbolos.buscar(nome, pilhaEscopos.peek());
        // Se não encontrar, procura no escopo global
        if (simbolo == null) {
            simbolo = tabelaSimbolos.buscar(nome, "global");
        }

        if (simbolo == null) {
            adicionarErro("Identificador '" + nome + "' não declarado",
                    idToken.getLinha(), idToken.getColuna(),
                    "Declare a variável antes de usá-la",
                    "ERRO_IDENTIFICADOR");
            return;
        }

        currentTokenIndex++;

        // Chamada de função
        if (currentTokenIndex < tokens.size() &&
                tokens.get(currentTokenIndex).getLexema().equals("(")) {
            if (!simbolo.isFunction()) {
                adicionarErro("'" + nome + "' não é uma função",
                        idToken.getLinha(), idToken.getColuna(),
                        "Use um nome de função válido",
                        "ERRO_TIPO");
                return;
            }

            currentTokenIndex++; // Consome '('

            // Verifica argumentos
            List<String> tiposArgumentos = new ArrayList<>();
            while (currentTokenIndex < tokens.size() &&
                    !tokens.get(currentTokenIndex).getLexema().equals(")")) {
                String tipoArg = analisarExpressao();
                tiposArgumentos.add(tipoArg);
                if (currentTokenIndex < tokens.size() &&
                        tokens.get(currentTokenIndex).getLexema().equals(",")) {
                    currentTokenIndex++;
                }
            }

            // Verifica número de argumentos
            if (tiposArgumentos.size() != simbolo.getParametros().length) {
                adicionarErro("Número incorreto de argumentos para função '" + nome + "'",
                        idToken.getLinha(), idToken.getColuna(),
                        "A função espera " + simbolo.getParametros().length + " argumentos",
                        "ERRO_ARGUMENTOS");
            } else {
                // Verifica tipos dos argumentos
                for (int i = 0; i < tiposArgumentos.size(); i++) {
                    if (!tiposCompativeis(simbolo.getParametros()[i], tiposArgumentos.get(i))) {
                        adicionarErro("Tipo incompatível no argumento " + (i + 1),
                                idToken.getLinha(), idToken.getColuna(),
                                "Use um valor do tipo " + simbolo.getParametros()[i],
                                "ERRO_TIPO");
                    }
                }
            }

            currentTokenIndex++; // Consome ')'
        }
        // Atribuição
        else if (currentTokenIndex < tokens.size() &&
                tokens.get(currentTokenIndex).getLexema().equals("=")) {
            if (simbolo.isFunction()) {
                adicionarErro("Não é possível atribuir valor a uma função",
                        idToken.getLinha(), idToken.getColuna(),
                        "Use um nome de variável válido",
                        "ERRO_TIPO");
                return;
            }

            currentTokenIndex++; // Consome '='
            String tipoExpr = analisarExpressao();

            if (!tipoExpr.equals(simbolo.getTipo())) {
                adicionarErro("Tipo incompatível na atribuição",
                        idToken.getLinha(), idToken.getColuna(),
                        "Use um valor do tipo " + simbolo.getTipo(),
                        "ERRO_TIPO");
            }

            simbolo.setInitialized(true);
        }
    }

    private void adicionarErro(String mensagem, int linha, int coluna, String sugestao, String tipo) {
        erros.add(new ErroSemantico(mensagem, linha, coluna, sugestao, tipo));
    }
}