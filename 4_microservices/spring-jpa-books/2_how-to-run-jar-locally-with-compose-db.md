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
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/db_bookstore \
SPRING_DATASOURCE_USERNAME=root \
SPRING_DATASOURCE_PASSWORD=secret \
java -jar target/bookstore-app.jar
```

## Observação importante

Quando a aplicação roda localmente (fora de container), o host do banco deve ser `localhost`.
O host `mysql` é usado quando a aplicação também está rodando em container na mesma rede Docker.
