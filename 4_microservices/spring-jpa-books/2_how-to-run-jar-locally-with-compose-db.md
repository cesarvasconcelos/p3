# Como rodar o JAR localmente usando apenas o MySQL do Docker

Este guia é para quando você quer estudar ou executar a aplicação **fora de container** (e fora do IntelliJ), usando `java -jar`.

## Cenário

Se você quer rodar a app fora de container (e fora do IntelliJ), o ideal é:

1. Subir só o banco com `compose.yaml`
2. Rodar o executável JAR localmente no terminal

## Passo 1: subir somente o MySQL

```bash
cd 4_microservices/spring-jpa-books
docker compose up -d mysql
```

## Passo 2: executar a aplicação com `java -jar`

Antes de executar, garanta que você está no diretório do projeto e que o JAR foi gerado:

```bash
cd ~/github/p3/4_microservices/spring-jpa-books
./mvnw clean package -DskipTests
```

```bash
SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/db_bookstore?useSSL=false&allowPublicKeyRetrieval=true" \
SPRING_DATASOURCE_USERNAME=root \
SPRING_DATASOURCE_PASSWORD=secret \
java -jar target/bookstore-app.jar
```

## Alternativa: passar como argumentos na linha de comando

O resultado é o mesmo. Em vez de variáveis de ambiente, usamos propriedades do Spring (`--chave=valor`) direto no comando:

```bash
java -jar target/bookstore-app.jar \
  --spring.datasource.url="jdbc:mysql://localhost:3306/db_bookstore?useSSL=false&allowPublicKeyRetrieval=true" \
  --spring.datasource.username=root \
  --spring.datasource.password=secret
```

> **Atenção às aspas na URL.** Sem elas o comando quebra: no `zsh` o `?` é interpretado como curinga
> (`no matches found`) e, no `bash`, o `&` manda o comando para background e corta a URL.
> Escapar só o `&` com `\&` **não** resolve no `zsh`; use aspas.

Ambas as formas têm precedência sobre o `application.properties` (argumentos de linha de comando > variáveis de ambiente > arquivo).

## Observação importante

Quando a aplicação roda localmente (fora de container), o host do banco deve ser `localhost`.
O host `mysql` é usado quando a aplicação também está rodando em container na mesma rede Docker.

Ao sobrescrever a URL, ela **substitui a URL inteira**, inclusive os parâmetros do `application.properties`.
Por isso `useSSL=false&allowPublicKeyRetrieval=true` precisam ser repetidos. Sem `allowPublicKeyRetrieval=true`,
com `useSSL=false` e o MySQL 8.4 recém-criado, a app falha com `Public Key Retrieval is not allowed`.

## Como parar tudo

### 1. Parar a aplicação (graceful shutdown)

No terminal onde o `java -jar` está rodando, pressione `Ctrl + C`.

Isso envia um `SIGINT` para a JVM, que dispara o *shutdown hook* do Spring Boot: a aplicação
finaliza as requisições em andamento, fecha o pool de conexões e encerra de forma limpa.
Evite `kill -9`, pois ele mata o processo sem dar chance de liberar os recursos.

### 2. Derrubar o MySQL e remover o volume

```bash
docker compose down -v
```

O `down` para e remove os containers e a rede. A flag `-v` também remove os **volumes**, ou seja,
**todos os dados do banco são apagados**. Na próxima vez que subir o MySQL, ele começa do zero.
Se quiser preservar os dados, use apenas `docker compose down`.

## Informação complementar: por que o `java -jar` não sobe o MySQL sozinho?

Seria natural esperar que o `java -jar` também iniciasse o container, já que o **Docker Compose Support**
está no `pom.xml`. Mas ele **só funciona em tempo de desenvolvimento**: não vai para dentro do JAR
executável, e isso é intencional.

### Por que o `java -jar` não sobe o container

No `pom.xml`, a dependência está declarada assim:

```xml
<artifactId>spring-boot-docker-compose</artifactId>
<scope>runtime</scope>
<optional>true</optional>
```

O `spring-boot-maven-plugin`, quando gera o fat JAR no `repackage`, **exclui por padrão** o
`spring-boot-docker-compose`, assim como faz com o `devtools`. Isso é controlado pela opção
`excludeDockerCompose`, que vem `true` por padrão. Dá para confirmar: o `target/bookstore-app.jar`
não contém nenhuma classe do docker-compose.

```bash
unzip -l target/bookstore-app.jar | grep -i docker-compose   # não retorna nada
```

Então, ao rodar `java -jar target/bookstore-app.jar`, o código que procuraria o `compose.yaml` e
executaria o `docker compose up` simplesmente não está no classpath. A aplicação tenta conectar no
MySQL em `localhost:3306` e falha, porque ninguém subiu o banco.

### Para que ele serve, então

Ele serve para o **ciclo de desenvolvimento**, quando a aplicação roda pelo Maven ou pela IDE:

```bash
./mvnw spring-boot:run      # ou o "Run" da IDE na classe main
```

Nesses casos a dependência está no classpath, e o Spring Boot faz o seguinte:

1. Encontra o `compose.yaml` no diretório do projeto.
2. Executa `docker compose up` antes de iniciar o contexto.
3. Lê as portas e variáveis dos containers e **cria a conexão automaticamente** (`ConnectionDetails`).
   Por isso não é preciso informar `spring.datasource.url/username/password`.
4. Ao encerrar a app, para os containers (conforme o `spring.docker.compose.lifecycle-management`;
   há um exemplo comentado no `application.properties`).

### Por que o Spring escolheu assim

Um JAR é um **artefato de deploy**. Em produção, ou dentro de um container Docker como no próximo
passo deste módulo, a aplicação não deve sair subindo a própria infraestrutura. O banco é provisionado
por fora (compose, Kubernetes, serviço gerenciado), e a app só recebe a URL e as credenciais. Por isso
este guia pede os dois passos separados:

```bash
docker compose up -d mysql
java -jar target/bookstore-app.jar --spring.datasource.url=...
```

| Forma de execução | Docker Compose Support ativo? | Quem sobe o MySQL? |
|---|---|---|
| `./mvnw spring-boot:run` / IDE | ✅ Sim | O Spring Boot, automaticamente |
| `./mvnw test` | ❌ Não, desativado em testes por padrão | Testcontainers (módulo 3) |
| `java -jar app.jar` | ❌ Não, excluído do JAR | Você (`docker compose up -d mysql`) |

> Dá para forçar a inclusão no JAR com `<excludeDockerCompose>false</excludeDockerCompose>` na
> configuração do `spring-boot-maven-plugin`, mas isso não é recomendado e foge do objetivo
> didático de separar desenvolvimento de deploy.
