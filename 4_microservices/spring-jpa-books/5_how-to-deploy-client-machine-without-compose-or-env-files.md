# Como implantar a Bookstore App (microsserviços) na máquina do cliente com Docker e banco de dados (sem Docker Compose e sem .env)

Este guia mostra uma forma simples de implantar a Bookstore App na máquina do cliente, com um banco de dados
externo, usando apenas o comando `docker run`.

- Objetivo:
    - executar um contêiner de banco de dados, executar o contêiner da aplicação e fazer a aplicação
      se conectar ao banco por meio de uma rede Docker privada.

> **Nota:** substitua `cesarvasconcelos/bookstore-app:1.0` pelo nome da sua imagem no [Docker Hub](https://hub.docker.com/).
> O prefixo `cesarvasconcelos` é o **seu usuário** do Docker Hub.

## O que precisa ser copiado para o cliente

**Nenhum arquivo.** O cliente só precisa de:

| Item | Como chega ao cliente |
|---|---|
| Docker (Engine ou Desktop) | instalado no cliente |
| Imagem `cesarvasconcelos/bookstore-app:1.0` | baixada automaticamente do Docker Hub |
| Imagem `mysql:8.4` | baixada automaticamente do Docker Hub |
| Os comandos abaixo | você os copia e cola no terminal do cliente |

## Comandos em ordem

```bash
# --- Na máquina do desenvolvedor ---
./mvnw clean package -DskipTests                                   # 1) gera o JAR
docker build -t bookstore-app:1.0 .                                # 2) constrói a imagem
docker tag bookstore-app:1.0 cesarvasconcelos/bookstore-app:1.0    # 3) nomeia para o Docker Hub
docker login                                                       # 4) autentica
docker push cesarvasconcelos/bookstore-app:1.0                     # 5) publica

# --- No cliente (apenas o Docker instalado) ---
docker volume create vol_bookstore                                 # 6) volume para os dados do MySQL
docker network create --driver=bridge bookstore-network            # 7) rede para os contêineres se acharem pelo nome

docker run -d --name db --network bookstore-network \
    -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=db_bookstore \
    -e MYSQL_USER=cesar -e MYSQL_PASSWORD=cesar123 \
    -v vol_bookstore:/var/lib/mysql mysql:8.4                      # 8) sobe o banco

docker logs -f db                                                  # 9) espere "ready for connections" (Ctrl+C sai)

docker run -d --name bookstore-app --network bookstore-network -p 8080:8080 \
    -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/db_bookstore \
    -e SPRING_DATASOURCE_USERNAME=root -e SPRING_DATASOURCE_PASSWORD=secret \
    cesarvasconcelos/bookstore-app:1.0                             # 10) sobe a aplicação

# 11) acesse http://localhost:8080

# --- Para limpar tudo no cliente ---
docker rm -f bookstore-app db                                      # remove os contêineres
docker network rm bookstore-network                                # remove a rede
docker volume rm vol_bookstore                                     # remove os dados (opcional)
```

Nas seções seguintes, cada passo é explicado em detalhes.

## Pré-requisitos

Para publicar a Bookstore App no Docker Hub, primeiro é preciso empacotar e _containerizar_ a aplicação,
criando uma imagem Docker. Execute os comandos a partir da pasta do projeto (onde está o `pom.xml`):

- `./mvnw clean package -DskipTests` para gerar o JAR ignorando os testes e acelerando o build
  (veja [1_how-to-run-tests.md](./1_how-to-run-tests.md))
- `docker build -t bookstore-app:1.0 .` para criar a imagem Docker a partir do `Dockerfile`
  (veja [3_how-to-build-image-and-run.md](./3_how-to-build-image-and-run.md))
- `docker tag bookstore-app:1.0 cesarvasconcelos/bookstore-app:1.0` para dar à imagem o nome do seu repositório no Docker Hub
- [Opcional] `docker image ls` para listar as imagens da sua máquina

## Publicar a imagem no Docker Hub

- `docker login` para autenticar no Docker Hub
- `docker push cesarvasconcelos/bookstore-app:1.0` para enviar a imagem à sua conta no Docker Hub

> O repositório precisa ser **público** (ou o cliente precisa executar `docker login` com uma conta com acesso)
> para que o `docker run` consiga baixar a imagem.

Com a imagem disponível no Docker Hub, você pode instalar o Docker no cliente.

## Instalar o Docker no cliente

Na máquina onde a aplicação será implantada, acesse o [Docker Desktop](https://www.docker.com/products/docker-desktop)
e baixe o instalador para o seu sistema operacional. Execute o instalador e siga as instruções.

> Em servidores Linux, também é possível instalar apenas o Docker Engine (sem o Docker Desktop).

## Subir um banco de dados externo em um contêiner

Execute um contêiner de banco de dados e aponte a aplicação para ele por meio de variáveis de ambiente.

### Exemplo com MySQL

1) Crie um volume Docker para persistir os dados do banco. Isso é fundamental: sem ele, os dados são perdidos quando o contêiner é removido.

    - `docker volume create vol_bookstore` para criar o volume que persistirá os dados do banco

2) Crie uma rede Docker para que os contêineres se comuniquem pelo nome:

    - `docker network create --driver=bridge bookstore-network` para criar a rede que conectará o contêiner da aplicação e o do banco

3) Inicie o contêiner do banco de dados, anexando o volume:

    ```bash
    docker run -d --name db \
        --network bookstore-network \
        -e MYSQL_ROOT_PASSWORD=secret \
        -e MYSQL_DATABASE=db_bookstore \
        -e MYSQL_USER=cesar \
        -e MYSQL_PASSWORD=cesar123 \
        -p 3306:3306 \
        -v vol_bookstore:/var/lib/mysql \
        mysql:8.4
    ```
    Esse comando executa o contêiner do banco de dados e baixa a imagem caso ela ainda não exista na máquina do cliente.

    > A opção `-p 3306:3306` é **opcional**: ela só serve para acessar o banco a partir da máquina hospedeira
    > (por exemplo, com o MySQL Workbench). A aplicação acessa o banco pela rede Docker e não precisa dela.
    > Remova-a se não precisar do acesso externo ou se a porta 3306 já estiver em uso.

4) Verifique o status do contêiner do banco de dados:
    - `docker logs db` para ver os logs do banco
    - `docker logs -f --tail=0 db` para acompanhar apenas os logs novos. `-f` (*follow*) faz o comando não terminar e aguardar novas mensagens de log; `--tail=0` indica que não queremos ver as mensagens anteriores, apenas as novas.

5) Aguarde de 10 a 20 segundos para o banco inicializar (nos logs, procure a mensagem `ready for connections`) e, então, inicie a aplicação:
    ```bash
    docker run -d --name bookstore-app \
        --network bookstore-network \
        -p 8080:8080 \
        -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/db_bookstore \
        -e SPRING_DATASOURCE_USERNAME=root \
        -e SPRING_DATASOURCE_PASSWORD=secret \
        cesarvasconcelos/bookstore-app:1.0
    ```
    Esse comando executa o contêiner da Bookstore App e baixa a imagem do Docker Hub caso ela ainda não exista na máquina do cliente.

    > Se a aplicação encerrar com erro de conexão porque o banco ainda não estava pronto, basta iniciá-la de novo: `docker start bookstore-app`.

6) Verifique o status do contêiner da aplicação:
    - `docker logs bookstore-app` para ver os logs da aplicação
    - `docker logs -f --tail=0 bookstore-app` para acompanhar apenas os logs novos da aplicação
    - `docker ps -a` para listar os contêineres e seus status

7) Abra a aplicação:
    - Acesse: http://localhost:8080

## Dica: como usar arquivos .env com `docker run`

Esta alternativa mantém credenciais e configurações em arquivos `.env`, em vez de escrever `-e CHAVE=VALOR` direto nos comandos. Ela centraliza os valores sensíveis, facilita a troca de senhas e deixa os comandos `docker run` mais limpos.

### 1) Crie os arquivos de ambiente no cliente

Crie dois arquivos na mesma pasta em que executará os comandos (ou use caminhos absolutos). Os comentários explicam cada configuração.

`db.env`

```properties
# -------- Configuração do contêiner MySQL --------
# Senha do root, para acesso administrativo
MYSQL_ROOT_PASSWORD=secret

# Nome do banco da aplicação, criado automaticamente na primeira inicialização
MYSQL_DATABASE=db_bookstore

# Usuário comum do banco, criado automaticamente com privilégios sobre MYSQL_DATABASE
MYSQL_USER=cesar
MYSQL_PASSWORD=cesar123
```

`app.env`

```properties
# -------- Configuração do Datasource do Spring --------
# Use o nome do contêiner 'db' como host, por meio da rede Docker
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/db_bookstore

# Prefira o usuário comum definido em db.env
# Se quiser usar o root, altere estas duas linhas.
SPRING_DATASOURCE_USERNAME=cesar
SPRING_DATASOURCE_PASSWORD=cesar123
```

> Em arquivos passados com `--env-file`, **não use aspas** nos valores e **não deixe comentários na mesma linha**
> do valor: o Docker os trata como parte do valor. Comentários devem ficar em linhas próprias, como acima.

### 2) Crie o volume e a rede (mesmos passos de antes)

Se você já criou o volume e a rede na seção anterior, pule este passo (o Docker acusará erro se já existirem).

```bash
docker volume create vol_bookstore
docker network create --driver=bridge bookstore-network
```

> Se os contêineres `db` e `bookstore-app` da seção anterior ainda existirem, remova-os antes para liberar os nomes
> e as portas: `docker rm -f bookstore-app db`. O volume `vol_bookstore` mantém os dados do banco já criado.
> Atenção: como o volume já foi inicializado com as credenciais antigas, o MySQL **ignora** as variáveis
> `MYSQL_*` do `db.env`. Para recomeçar do zero, remova também o volume: `docker volume rm vol_bookstore`.

### 3) Execute o banco de dados com `--env-file`

Pronto para copiar e colar:

```bash
# Executa um contêiner MySQL 8.4:
# - conecta-se à rede 'bookstore-network' para que a aplicação o alcance pelo nome de host 'db'
# - publica a porta 3306 para ferramentas locais (opcional; remova se não precisar de acesso pela máquina hospedeira)
# - monta o volume 'vol_bookstore' para persistir os dados entre reinicializações do contêiner
# - carrega as credenciais e configurações do banco a partir de db.env
docker run -d --name db \
  --network bookstore-network \
  -p 3306:3306 \
  -v vol_bookstore:/var/lib/mysql \
  --env-file ./db.env \
  mysql:8.4
```
```diff
# Nota: em vez de várias opções -e, use um único arquivo de ambiente
-    -e MYSQL_ROOT_PASSWORD=secret \
-    -e MYSQL_DATABASE=db_bookstore \
-    -e MYSQL_USER=cesar \
-    -e MYSQL_PASSWORD=cesar123 \
+    --env-file ./db.env \
```

### 4) Execute a aplicação com `--env-file`

Aguarde de 10 a 20 segundos para o MySQL inicializar e, então, inicie a aplicação.

Pronto para copiar e colar:

```bash
# Executa a Bookstore App (Spring Boot):
# - conecta-se à rede 'bookstore-network' para alcançar o MySQL pelo host 'db'
# - publica a porta 8080 para acesso pelo navegador na máquina hospedeira
# - carrega a URL do datasource e as credenciais a partir de app.env
docker run -d --name bookstore-app \
  --network bookstore-network \
  -p 8080:8080 \
  --env-file ./app.env \
  cesarvasconcelos/bookstore-app:1.0
```
```diff
# Nota: em vez de várias opções -e, use um único arquivo de ambiente
-    -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/db_bookstore \
-    -e SPRING_DATASOURCE_USERNAME=root \
-    -e SPRING_DATASOURCE_PASSWORD=secret \
+    --env-file ./app.env \
```

### 5) Verifique e abra a aplicação

```bash
docker logs db
docker logs bookstore-app
docker ps -a
```

Acesse: `http://localhost:8080`

### Notas e dicas

- Caminhos relativos e absolutos: `--env-file` aceita caminhos relativos ou absolutos (por exemplo, `--env-file /home/usuario/config/db.env`).
- Segurança: mantenha os arquivos `.env` fora do controle de versão e dos backups sem proteção. Prefira usuários comuns (não root) para a aplicação.
- Para usar o root (se necessário): defina `SPRING_DATASOURCE_USERNAME=root` e `SPRING_DATASOURCE_PASSWORD=secret` no `app.env`.
- Comportamento na primeira execução: com `MYSQL_DATABASE`, `MYSQL_USER` e `MYSQL_PASSWORD`, o MySQL cria o banco e concede os privilégios apenas na primeira inicialização; as próximas inicializações reaproveitam os dados de `vol_bookstore`.
- Para parar e limpar tudo: `docker rm -f bookstore-app db`, `docker network rm bookstore-network` e, **se quiser apagar os dados**, `docker volume rm vol_bookstore`.

## Observação final: orquestração em produção

O Docker é um ótimo motor de contêineres para desenvolvimento e empacotamento, mas, em produção, você deve executar os contêineres sob um orquestrador, que mantém os serviços saudáveis, escala entre vários servidores e garante alta disponibilidade.

> <q>Um motor de contêineres, como o Docker, não é suficiente para produção. É necessário um orquestrador para manter os contêineres em execução e escalá-los entre servidores, oferecendo alta disponibilidade e mais recursos computacionais.</q>

Isso está alinhado à orientação do livro recente “Microservices with Spring Boot and Spring Cloud (edição 2025)”. Referência: https://www.amazon.com.br/Microservices-Spring-Boot-Cloud-microservices-dp-1805801279/dp/1805801279/ref=dp_ob_title_bk

Orquestradores comuns incluem Kubernetes (o mais adotado), Amazon ECS, HashiCorp Nomad e plataformas semelhantes. Em demonstrações de sala de aula, o `docker run` é suficiente; em produção, prefira um orquestrador.
