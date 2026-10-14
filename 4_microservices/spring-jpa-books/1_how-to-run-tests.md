# Como executar os testes

> **Lembrete:** a máquina pode não ter o Maven configurado no `PATH`. Nesse caso, use o **Maven Wrapper** (`./mvnw`) incluído no projeto no lugar de `mvn`. Todos os comandos abaixo funcionam com `./mvnw` (no Windows, use `mvnw.cmd`).
>
> Execute os comandos sempre a partir da pasta do projeto (onde está o `pom.xml`).

## Como empacotar a aplicação sem executar os testes

- `./mvnw clean package -DskipTests` para **empacotar** e gerar o arquivo Jar **sem** executar os testes
- Verifique a pasta `target/` para confirmar se o arquivo Jar foi criado.

### Executando os testes
- `./mvnw clean test -Dspring.profiles.active=test` para executar os testes

### Executando apenas uma classe de teste
Use o `-Dtest` do Maven Surefire com o profile `test` ativado:

- Executar apenas `BookControllerIntegrationTest`:
  - `./mvnw -Dspring.profiles.active=test -Dtest=com.example.books.integration.BookControllerIntegrationTest test`

- Executar apenas `BookRepositoryUsingSpringTestContainersSupportTest`:
  - `./mvnw -Dspring.profiles.active=test -Dtest=com.example.books.repository.BookRepositoryUsingSpringTestContainersSupportTest test`

Dica: você também pode executar um único método de teste adicionando `#nomeDoMetodo`:
- `./mvnw -Dspring.profiles.active=test -Dtest=com.example.books.integration.BookControllerIntegrationTest#givenAuthenticatedUser_whenPostBook_thenItIsSavedAndRedirects test`

> Se o `./mvnw` retornar "Permission denied" no Linux/macOS, torne-o executável com `chmod +x mvnw`.
