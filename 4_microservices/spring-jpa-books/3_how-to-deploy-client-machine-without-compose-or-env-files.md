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
