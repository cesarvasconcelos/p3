# How to Deploy the Microservice Bookstore App with Docker and Database (No Docker Compose, No .env)

This guide shows a simple way to deploy the microservice Bookstore App on the client machine with an external
database, using only `docker run` command.

- Objective:
    - run a database container, run the app container, and point the app to it using a private network.

Note: you must replace `cesarvasconcelos/bookstore-app:1.0` with your [Docker Hub](https://hub.docker.com/) image name.

## Prerequisites
To publish your Bookstore App on Docker Hub, first you need to package and _containerize_ the
Bookstore  app by creating a Docker image:
- `mvn clean package -DskipTests` to skip tests and speed up the build process
- `docker build -t bookstore-app:1.0 .` to create a Docker image from Dockerfile
- `docker tag bookstore-app:1.0 cesarvasconcelos/bookstore-app:1.0` to tag the image with your Docker Hub image name
- [Optional] `docker image ls` to list the images on your machine

## Publish the Image on Docker Hub
- `docker login` to log in to Docker Hub
- `docker push cesarvasconcelos/bookstore-app:1.0` to push the image to your Docker Hub account

Now that the image is available on Docker Hub, you can install Docker Desktop on the client.

## Install Docker Desktop on the client

On the computer where the app will be deployed, go to [Docker Desktop](https://www.docker.com/products/docker-desktop) and download the installer for your operating system.
Run the installer and follow the instructions to install Docker Desktop.

## Spin Up an External DB via a Database Container
Run a database container and point the app at it via environment variables.

### MySQL Example
1) Create a Docker volume to persist database data. This is critical—without it, your data is lost when the container is removed.

    - `docker volume create vol_bookstore`  to create a volume to persist database data

2) Create a Docker network so containers can talk by name:
    - `docker network create --driver=bridge bookstore-network`  to create a network to connect the app and the database containers

3) Start the database container, attaching the volume:

    ```bash
    docker run -d --name db \
        --network bookstore-network \
        -e MYSQL_ROOT_PASSWORD=secret \
        -e MYSQL_DATABASE=db_bookstore \
        -e MYSQL_USER=cesar \
        -e MYSQL_PASSWORD=cesar123 \
        -p 3306:3306 \
        -v vol_bookstore:/var/lib/mysql \
        mysql:8.1
    ```
    To run the database container. This command will download the image if it's not already available on the client machine.


4) Check the Database Container Status
- `docker logs db` to check the database logs

5) Wait 10–20 seconds for the DB to initialize, then start the app:
    ```bash
    docker run -d --name bookstore-app \
        --network bookstore-network \
        -p 8080:8080 \
        -e SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/db_bookstore \
        -e SPRING_DATASOURCE_USERNAME=root \
        -e SPRING_DATASOURCE_PASSWORD=secret \
        cesarvasconcelos/bookstore-app:1.0
    ```
    To run the bookstore app container. This command will download the image if it's not already available on the client machine.

6) Check the App Container Status
- `docker logs bookstore-app` to check the app logs
- `docker ps -a` to check the running containers

7) Open the app:
- Visit: http://localhost:8080

## Tip: How to Use .env Files with `docker run`

This alternative keeps credentials and configuration in `.env` files instead of hardcoding `-e KEY=VALUE` in commands. It centralizes sensitive values, makes rotation easier, and keeps your `docker run` lines clean.

### 1) Create environment files on the client

Create two files side by side with your commands (or use absolute paths). Comments below explain each setting.

`db.env`

```properties
# -------- MySQL container configuration --------
# Root password for administrative access
MYSQL_ROOT_PASSWORD=secret

# Application database name to auto-create on first start
MYSQL_DATABASE=db_bookstore

# Non-root database user to auto-create with privileges on MYSQL_DATABASE
MYSQL_USER=cesar
MYSQL_PASSWORD=cesar123
```

`app.env`

```properties
# -------- Spring Datasource configuration --------
# Use the container name 'db' as the host through the Docker network
SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/db_bookstore

# Prefer the non-root user defined in db.env
# If you intentionally want to use root instead, change these two lines.
SPRING_DATASOURCE_USERNAME=cesar
SPRING_DATASOURCE_PASSWORD=cesar123
```

### 2) Create volume and network (same steps as above)

```bash
docker volume create vol_bookstore
docker network create --driver=bridge bookstore-network
```

### 3) Run the database with `--env-file`

Copy-paste ready:

```bash
# Run a MySQL 8.1 container:
# - attaches to 'bookstore-network' so the app can reach it by the host name 'db'
# - publishes port 3306 for local tools (optional; remove if you don't need host access)
# - mounts 'vol_bookstore' to persist data across container restarts
# - loads DB credentials and settings from db.env
docker run -d --name db \
  --network bookstore-network \
  -p 3306:3306 \
  -v vol_bookstore:/var/lib/mysql \
  --env-file ./db.env \
  mysql:8.1
```

### 4) Run the app with `--env-file`

Wait 10–20 seconds for MySQL to initialize, then start the app.

Copy-paste ready:

```bash
# Run the Spring Boot bookstore app:
# - attaches to 'bookstore-network' so it can connect to MySQL at host 'db'
# - publishes port 8080 for browser access on the host
# - loads datasource URL and credentials from app.env
docker run -d --name bookstore-app \
  --network bookstore-network \
  -p 8080:8080 \
  --env-file ./app.env \
  cesarvasconcelos/bookstore-app:1.0
```

### 5) Verify and open the app

```bash
docker logs db
docker logs bookstore-app
docker ps -a
```

Visit: `http://localhost:8080`

### Notes and tips

- Relative vs absolute paths: `--env-file` accepts relative or absolute paths (e.g., `--env-file /home/user/config/db.env`).
- Security: keep `.env` files out of source control and secure backups. Prefer non-root DB users for the app.
- Switching to root (if required): set `SPRING_DATASOURCE_USERNAME=root` and `SPRING_DATASOURCE_PASSWORD=secret` in `app.env`.
- First-run behavior: with `MYSQL_DATABASE`, `MYSQL_USER`, and `MYSQL_PASSWORD`, MySQL initializes the database and grants privileges on the first start; subsequent starts reuse data in `vol_bookstore`.
