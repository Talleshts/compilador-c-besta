# Compiladores Backend

Este projeto é a parte do backend do projeto de compiladores C--. Ele é construído usando Spring Boot.

## Pré-requisitos

Antes de começar, certifique-se de ter os seguintes softwares instalados em sua máquina:

- [Java JDK 17](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
- [Maven](https://maven.apache.org/install.html)
- [Git](https://git-scm.com/downloads)

## Passos para configurar o ambiente

### 1. Clonar o repositório

Primeiro, clone o repositório do projeto para sua máquina local usando o comando:

```sh
git clone <URL_DO_REPOSITORIO>
```

### 2. Navegar até o diretório do backend

Entre no diretório do backend:

```sh
cd backend
```

### 3. Configurar variáveis de ambiente

Certifique-se de que a variável de ambiente `JAVA_HOME` está configurada para apontar para o JDK 17. Você pode configurar isso no Windows da seguinte forma:

1. Abra o Painel de Controle.
2. Vá para Sistema e Segurança > Sistema > Configurações avançadas do sistema.
3. Clique em Variáveis de Ambiente.
4. Em Variáveis do sistema, clique em Novo e adicione `JAVA_HOME` com o caminho para o JDK 17 (por exemplo, `C:\Program Files\Java\jdk-17`).
5. Adicione `%JAVA_HOME%\bin` ao final da variável `Path`.

### 4. Compilar o projeto

Execute o comando abaixo para compilar o projeto e baixar todas as dependências necessárias:

```sh
mvn clean install
```

### 5. Rodar o backend

Após a compilação bem-sucedida, você pode iniciar o servidor backend com o seguinte comando:

```sh
mvn spring-boot:run
```

O servidor será iniciado e estará disponível em [http://localhost:8080](http://localhost:8080).

## Endpoints disponíveis

### Hello Endpoint

- **URL:** `http://localhost:8080/api/hello`
- **Método HTTP:** `GET`
- **Descrição:** Retorna uma mensagem de saudação do backend.

## Estrutura do Projeto

- `src/main/java/com/ufes/compiladores`: Contém o código fonte principal do projeto.
- `src/main/resources`: Contém os arquivos de configuração e recursos estáticos.
- `src/test/java/com/ufes/compiladores`: Contém os testes unitários do projeto.

## Problemas comuns

### Erro de versão do Java

Se você encontrar um erro relacionado à versão do Java, certifique-se de que está usando o JDK 17 e que a variável de ambiente `JAVA_HOME` está configurada corretamente.

### Porta em uso

Se a porta 8080 já estiver em uso, você pode alterar a porta do servidor Spring Boot no arquivo `application.properties`:

```properties
server.port=8081
```

Substitua `8081` pela porta desejada.

## Contribuição

Se você deseja contribuir com este projeto, por favor, faça um fork do repositório e envie um pull request com suas alterações.

## Licença

Este projeto é licenciado sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.
