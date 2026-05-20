# Deploy no cliente com Docker Compose (simples)

No cliente Linux, crie um arquivo `compose.yaml` com o conteúdo abaixo:

```yaml
services:
  db:
    image: mysql:8.1
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
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 10
    networks:
      - bookstore-network

  bookstore-app:
    image: cesarvasconcelos/bookstore-app:1.0
    container_name: bookstore-app
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
