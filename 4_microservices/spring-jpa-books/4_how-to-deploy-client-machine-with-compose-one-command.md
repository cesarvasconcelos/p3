# Deploy no cliente com Docker Compose (simples)

## O que precisa ser copiado para o cliente

| Item | Como chega ao cliente |
|---|---|
| `compose.yaml` (conteúdo abaixo) | **você copia** para uma pasta do cliente |
| Imagem `cesarvasconcelos/bookstore-app:1.0` | baixada automaticamente do Docker Hub (publicada antes, nos passos 1 a 4 abaixo) |
| Imagem `mysql:8.4` | baixada automaticamente do Docker Hub |
| Docker (Engine ou Desktop) com Compose | instalado no cliente |

Nenhum `.env`, JAR ou código-fonte precisa ir para o cliente.

## Comandos em ordem

```bash
# 1) Na máquina do desenvolvedor: gera o JAR
./mvnw clean package -DskipTests

# 2) Na máquina do desenvolvedor: constrói a imagem
docker build -t bookstore-app:1.0 .

# 3) Na máquina do desenvolvedor: dá à imagem o nome do repositório no Docker Hub
docker tag bookstore-app:1.0 cesarvasconcelos/bookstore-app:1.0

# 4) Na máquina do desenvolvedor: autentica e publica a imagem
docker login
docker push cesarvasconcelos/bookstore-app:1.0

# 5) No cliente: crie a pasta e o compose.yaml (conteúdo na próxima seção)
mkdir bookstore && cd bookstore

# 6) No cliente: baixa as imagens, cria rede e volume e sobe os dois contêineres.
#    O bookstore-app só inicia depois que o MySQL estiver saudável (healthcheck).
docker compose up -d

# 7) No cliente: verifica o status e os logs
docker compose ps
docker compose logs -f bookstore-app

# 8) No cliente: acesse http://localhost:8080

# 9) No cliente: para tudo (use -v apenas se quiser apagar os dados do banco)
docker compose down
```

## O arquivo `compose.yaml`

No cliente Linux, crie um arquivo `compose.yaml` com o conteúdo abaixo:

```yaml
services:
  db:
    image: mysql:8.4
    container_name: db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: db_bookstore
      MYSQL_USER: cesar
      MYSQL_PASSWORD: cesar123
    ports:
      - "3306:3306"
    volumes:
      - vol_bookstore:/var/lib/mysql
    healthcheck:
      # 127.0.0.1 (TCP) e não localhost (socket): evita "saudável" durante o servidor temporário da 1ª inicialização
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 60s
    networks:
      - bookstore-network

  bookstore-app:
    image: cesarvasconcelos/bookstore-app:1.0
    container_name: bookstore-app
    # Reinicia o container se a app falhar (ex.: banco ainda indisponível) e também após reboot do host,
    # exceto se você o parou manualmente
    restart: unless-stopped
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/db_bookstore
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: secret
    ports:
      - "8080:8080"
    networks:
      - bookstore-network

volumes:
  vol_bookstore:
    name: vol_bookstore

networks:
  bookstore-network:
    name: bookstore-network
    driver: bridge
```

Depois, no mesmo diretório, rode um único comando:

```bash
docker compose up -d
```
