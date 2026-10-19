# Como containerizar a Bookstore App criando uma imagem Docker

Este guia mostra como:
1. Construir uma imagem pronta para distribuição que contém apenas o JAR executável do Spring Boot.
2. Executar a aplicação em um contêiner Docker usando um banco de dados externo (por exemplo, MySQL).
3. Implantar a aplicação em uma arquitetura de microsserviços:
   - `bookstore-app`: o serviço principal da aplicação, que usa o banco de dados.
   - `mysql`: o serviço de banco de dados.

> ### Resumo: como containerizar manualmente a aplicação criando uma imagem Docker
>
> 1. `./mvnw clean package -DskipTests` — gera o JAR em `target/`
> 2. `docker build -t bookstore-app:1.0 .` — cria a imagem Docker a partir do Dockerfile
> 3. `docker compose -f compose-deploy.yaml up -d` — sobe a stack (aplicação + MySQL)
> 4. `docker compose -f compose-deploy.yaml down -v` — derruba a stack e **remove os volumes** (os dados do MySQL são apagados)

## Empacotamento e Implantação com Docker

O projeto inclui um `Dockerfile` multi-stage e um `compose-deploy.yaml` que definem os serviços da aplicação e do banco de dados.

### Análise do Build Multi-stage (Dockerfile deste projeto)

- Estágio 1 (builder/extract): `bellsoft/liberica-openjre-debian:21-cds`
  - Copia o JAR do Spring Boot já construído (`target/*.jar`) para `application.jar`.
  - Usa `java -Djarmode=tools -jar application.jar extract --layers` para descompactar o JAR em `extracted/` (diretórios em camadas).
- Estágio 2 (runtime): `bellsoft/liberica-openjre-debian:21-cds`
  - Copia `dependencies/`, `spring-boot-loader/`, `snapshot-dependencies/` e `application/` do estágio builder.
  - Expõe a porta `8080` e inicia a aplicação com `ENTRYPOINT ["java", "-jar", "application.jar"]`.

Benefícios:
- Imagem de runtime menor e inicialização mais rápida.
- Cada camada é uma instrução `COPY` separada: ao alterar apenas o código da aplicação, o Docker reaproveita do cache as camadas de dependências.

Somado ao uso de convenção sobre configuração, o Spring Boot também favorece um modelo de execução baseado em um arquivo JAR independente, conhecido como **fat JAR**. Antes do Spring Boot, a forma mais comum de executar uma aplicação Spring era implantá-la como um arquivo WAR em um servidor web Java EE, como o Apache Tomcat. A implantação de WAR ainda é suportada pelo Spring Boot.

> **Note:** A fat JAR file contains not only the classes and resource files of the application itself but **also all the JAR files the application depends on.** This means that the fat JAR file is **the only JAR file required to run the application;** that is, we only need to transfer one JAR file to an environment where we want to run the application instead of transferring the application’s JAR file along with all the JAR files the application depends on.

> Starting a fat JAR requires no separately installed Java EE web server, such as Apache Tomcat. Instead, it can be started with a simple command such as `java -jar app.jar`, making it a perfect choice for running in a Docker container!
>
> — Magnus Larsson, [Microservices with Spring Boot & Spring Cloud, Fourth Edition](https://www.packtpub.com/en-us/product/microservices-with-spring-boot-and-spring-cloud-9781805801269)

### A. Construir a Imagem da Aplicação (via Dockerfile)

A seguir, as instruções passo a passo para construir, executar e gerenciar a stack usando Docker.

Use esta seção quando quiser construir a imagem da aplicação manualmente. O fluxo com Compose (seção B) também pode construí-la automaticamente.

```bash
# Navegue até o diretório do projeto
cd /Users/cesar/github/p3/4_microservices/spring-jpa-books

# Gere o arquivo JAR da aplicação (o Dockerfile copia o JAR de target/)
./mvnw clean package -DskipTests

# Construa a imagem da aplicação usando o Dockerfile, com o cache padrão:
# o .dockerignore exclui arquivos e diretórios do contexto de build
docker build -t bookstore-app:1.0 .
                                # ^ Este ponto significa "diretório atual"

# (Opcional) Liste as imagens para confirmar
docker images | grep bookstore-app

# (Opcional) Inspecione as camadas e os metadados da imagem
docker inspect bookstore-app:1.0

# (Opcional) Tags por versão (reaproveita a imagem já construída, sem novo build)
docker tag bookstore-app:1.0 bookstore-app:1.0.0
docker tag bookstore-app:1.0 bookstore-app:1

# (Opcional) Tag latest (desenvolvimento)
docker tag bookstore-app:1.0 bookstore-app:latest

# (Opcional) Tag baseada no Git (se estiver usando git)
docker tag bookstore-app:1.0 bookstore-app:$(git rev-parse --short HEAD)

# (Opcional) Tag baseada na data
docker tag bookstore-app:1.0 bookstore-app:$(date +%Y%m%d)

# (Opcional) Várias tags em um único comando de build
docker build -t bookstore-app:latest -t bookstore-app:1.0.0 .
```

Observações:
- O `Dockerfile` usa um build multi-stage, mas **ambos os estágios** usam Liberica OpenJRE Debian. O Maven **não** é executado dentro do Docker.
- Por isso o JAR precisa ser gerado **antes** do `docker build` (`./mvnw clean package`). Sem o JAR em `target/`, o `COPY` falha.
- Os testes são ignorados pelo `-DskipTests` no comando do Maven (não no Dockerfile). Execute os testes localmente, se necessário (`./mvnw test`).
- `docker tag` apenas cria um novo nome para a mesma imagem; refazer `docker build` para cada tag é desnecessário.

### Gerenciamento de Cache

- O comportamento padrão reutiliza camadas em cache sempre que possível para acelerar os builds.
- Build sem usar cache (força a reexecução de todas as etapas):
  - `docker build --no-cache -t bookstore-app:1.0 .`
- Atualizar as camadas das imagens base (útil quando as imagens base mudam):
  - `docker build --pull -t bookstore-app:1.0 .`
- Exibir saída detalhada (útil para depuração):
  - `docker build --no-cache --progress=plain -t bookstore-app:1.0 .`

Quando usar `--no-cache`:
- Não é necessário na maioria das iterações; o cache acelera os builds.
- Use quando as imagens base mudaram e você quer um rebuild garantidamente limpo.
- Use ao investigar camadas desatualizadas ou comportamento inesperado no build.

Quando usar `--progress=plain`:
- Use para ver o progresso detalhado, camada por camada, do build.
- Útil para depurar erros de build ou entender o comportamento do cache.

### B. Executar a Stack Completa com Docker Compose

O `compose-deploy.yaml` define dois serviços: `mysql` e `bookstore-app`. O serviço `bookstore-app` aguarda o `mysql` ficar saudável (*healthy*) antes de iniciar.

```bash
# Navegue até o diretório do projeto
cd /Users/cesar/github/p3/4_microservices/spring-jpa-books

# 1) Copie o template de variáveis de ambiente e ajuste os valores
#    (atenção: se o .env já existir, ele será sobrescrito)
cp .env.example .env
# Edite o .env com as senhas desejadas e, OBRIGATORIAMENTE, com o banco e a URL deste projeto:
#   MYSQL_DATABASE=db_bookstore
#   SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/db_bookstore
# O .env.example é apenas um modelo genérico (usa MariaDB e outro banco);
# sem esse ajuste, a aplicação não consegue se conectar ao MySQL.

# 2) Suba a stack completa (constrói a imagem bookstore-app se necessário;
#    lembre-se de gerar o JAR antes: ./mvnw clean package -DskipTests)
docker compose -f compose-deploy.yaml up -d

# 3) Verifique o status e a saúde dos serviços
docker compose -f compose-deploy.yaml ps -a

# 4) Conecte-se ao banco de dados
docker exec -it db mysql -uroot -p

# 5) Execute uma consulta em um único comando, sem abrir o cliente interativo
#    (-e executa o SQL e sai; -it é necessário para digitar a senha; o banco é o MYSQL_DATABASE do .env)
docker exec -it db mysql -uroot -p db_bookstore -e "SELECT * FROM tbl_book;"
# Alternativa passando a senha na linha de comando (evite fora de ambiente de estudo):
# docker exec db mysql -uroot -p'SUA_SENHA' db_bookstore -e "SELECT * FROM tbl_book;"
# Alternativa lendo a senha da variável já definida dentro do contêiner:
# docker exec db sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" -e "SELECT * FROM tbl_book;"'

# 6) Inspecione o contêiner do banco de dados
docker inspect db
```

Observações sobre o passo 5:
- Sem `-it`, o prompt de senha do `-p` não funciona. A terceira forma (`sh -c`) dispensa o `-it`, pois não pede nem expõe a senha: ela usa `MYSQL_ROOT_PASSWORD` e `MYSQL_DATABASE`, já definidas dentro do contêiner.
- Nas formas que passam a senha na linha de comando, o MySQL exibe o aviso `mysql: [Warning] Using a password on the command line interface can be insecure.`. Ele é esperado e não é um erro: a consulta é executada normalmente.
- A tabela é `tbl_book` (criada pela migração Flyway `V1__create-database.sql`) e o banco `db_bookstore` vem do `MYSQL_DATABASE` do `.env`; ajuste se você alterou o `.env`.

Acesse a aplicação:
```bash
curl http://localhost:8080/
```
