# How to containerize and deploy the app by creating a Docker image

It shows how to:
1. Build a shippable image that contains only the runnable Spring Boot JAR.
2. Run the application in a Docker container using an external database (e.g., MySQL).
3. The application is deployed in a microservices architecture:
   - `bookstore-app`: the main application service that uses the database.
   - `mysql`: the database service.

## Docker Packaging and Deployment

The project includes a multi-stage `Dockerfile` and a `compose-deploy.yml` that define the application and database services.

### Multi-stage Build Analysis (this project’s Dockerfile)

- Stage 1 (build): `maven:3.9.11-eclipse-temurin-21`
  - Copies `pom.xml` first to leverage dependency caching.
  - Runs `mvn dependency:go-offline` then copies `src/` and builds the JAR (`-DskipTests`).
- Stage 2 (runtime): `eclipse-temurin:21-jre`
  - Copies the built JAR, exposes `8080`, and uses `ENTRYPOINT` to start the app.

Benefits:
- Smaller runtime image, faster start-up.

### A. Build the Application Image (via Dockerfile)

Below are step-by-step instructions to build, run, and manage the stack using Docker.

Use this when you want to build the app image manually. The Compose workflow (section B) can also build it automatically.

```bash
# Navigate to the project directory
cd /Users/cesar/github/p3/3_testcontainers/spring-jpa-books

# Build the application JAR file
mvn clean package -DskipTests
# Build the application image using the Dockerfile with default cache:
# .dockerignore is used to exclude files and directories from the build context
docker build -t bookstore-app:1.0 .
                                # ^ This dot means "current directory"

# (Optional) List images to confirm
docker images | grep bookstore-app

# (Optional) Inspect image layers and metadata
docker inspect bookstore-app:1.0

# (Optional) Version-based tagging
docker build -t bookstore-app:1.0.0 .
docker build -t bookstore-app:1.0 .
docker build -t bookstore-app:1 .

# (Optional) Latest tag (development)
docker build -t bookstore-app:latest .

# (Optional) Git-based tagging (if using git)
docker build -t bookstore-app:$(git rev-parse --short HEAD) .

# (Optional) Date-based tagging
docker build -t bookstore-app:$(date +%Y%m%d) .

# (Optional) Multi-tag in single command
docker build -t bookstore-app:latest -t bookstore-app:1.0.0 .
```

Notes:
- The `Dockerfile` uses a multi-stage build: Maven (build) → Temurin JRE (runtime).
- Tests are skipped in the image build (`-DskipTests` in Dockerfile). Run tests locally if needed.

### Cache Management

- Default behavior uses cached layers when possible to speed up builds.
- Build without using cache (forces all steps to re-run):
  - `docker build --no-cache -t bookstore-app:1.0 .`
- Refresh base image layers (useful when base images change):
  - `docker build --pull -t bookstore-app:1.0 .`
- Show verbose output (useful for debugging):
  - `docker build --no-cache --progress=plain -t bookstore-app:1.0 .`

When to use `--no-cache`:
- Not required for most iterations; caching speeds up builds.
- Use it if base images changed and you want a known-clean rebuild.
- Use it when troubleshooting stale layers or unexpected build behavior.

When to use `--progress=plain`
- Use it to see detailed layer-by-layer build progress.
- Useful for debugging build errors or understanding cache behavior.


### Summary: how to manually containerize and the app by creating a Docker image
- `mvn clean package -DskipTests`
- `docker build -t bookstore-app:1.0 .` to create a Docker image from Dockerfile
- `docker-compose -f compose-deploy.yaml up -d`
- `docker-compose -f compose-deploy.yaml down -v`

### B. Run the Full Stack with Docker Compose

The `compose-deploy.yml` defines two services: `mysql` and `bookstore-app`. The `bookstore-app` service waits for `mysql` to be healthy before starting.

```bash
# Navigate to the project directory
cd /Users/cesar/github/p3/3_testcontainers/spring-jpa-books

# 1) Copy environment template and adjust values
cp .env.example .env
# Edit .env with your desired passwords and settings

# 2) Start the full stack (builds the bookstore-app image if needed)
docker-compose -f compose-deploy.yaml up -d

# 3) Check service status and health
docker-compose ps -a

```

Access the application:
```bash
curl http://localhost:8080/
```