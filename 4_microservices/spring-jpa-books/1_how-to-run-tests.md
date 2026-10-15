# Como executar os testes

> **Lembrete:** a máquina pode não ter o Maven configurado no `PATH`. Nesse caso, use o **Maven Wrapper** (`./mvnw`) incluído no projeto no lugar de `mvn`. Todos os comandos abaixo funcionam com `./mvnw` (no Windows, use `mvnw.cmd`).
>
> Execute os comandos sempre a partir da pasta do projeto (onde está o `pom.xml`).

## Como empacotar a aplicação sem executar os testes

- `./mvnw clean package -DskipTests` para **empacotar** e gerar o arquivo Jar **sem** executar os testes
- Verifique a pasta `target/` para confirmar se o arquivo Jar foi criado: `ls target/*.jar`

#### De onde vem o nome do arquivo Jar?

O nome é definido pela tag `<finalName>`, dentro de `<build>`, no `pom.xml`:

```xml
<build>
     <finalName>bookstore-app</finalName>
     ...
</build>
```

Por isso o Maven gera `target/bookstore-app.jar`. Sem essa tag, o Maven usaria o padrão `${artifactId}-${version}`, ou seja, `target/spring-jpa-books-1.0.jar`.

Um nome fixo, sem a versão, facilita usar o Jar em outros comandos, como `java -jar target/bookstore-app.jar`, que não precisam mudar quando a versão do projeto muda.

#### Por que aparece o `bookstore-app.jar.original`?

No `package`, o Maven gera primeiro um Jar comum, só com as classes do projeto. Em seguida, o objetivo `repackage` do `spring-boot-maven-plugin` renomeia esse Jar para `.original` e cria, no lugar dele, o *fat JAR* executável.

| | `bookstore-app.jar` | `bookstore-app.jar.original` |
|---|---|---|
| Conteúdo | classes do projeto + todas as dependências | apenas as classes do projeto |
| Tamanho (aprox.) | ~58 MB | ~27 KB |
| Executa com `java -jar`? | ✅ sim | ❌ não |

O `.original` pode ser ignorado. O `Dockerfile` copia `target/*.jar`, que não inclui o `.original`.

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
