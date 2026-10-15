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
