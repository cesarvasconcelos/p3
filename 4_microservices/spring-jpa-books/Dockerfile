# From Spring Official Docs: https://docs.spring.io/spring-boot/reference/packaging/container-images/dockerfiles.html
# Multi-stage build for Spring Boot application

# Stage 1: Build the application
# Perform the extraction in a separate builder container
FROM bellsoft/liberica-openjre-debian:21-cds AS builder
WORKDIR /builder
# This points to the built jar file in the target folder
# Adjust this to 'build/libs/*.jar' if you're using Gradle
ARG JAR_FILE=target/*.jar
# Copy the jar file to the working directory and rename it to application.jar
COPY ${JAR_FILE} application.jar
# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# Stage 2: Create the runtime image
# Smaller final image : Only JRE, not full JDK or Maven
# This is the runtime container
FROM bellsoft/liberica-openjre-debian:21-cds
WORKDIR /application
# Copy the extracted jar contents from the builder container into the working directory in the runtime container
# Every copy step creates a new docker layer
# This allows docker to only pull the changes it really needs
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# Expose the port the app runs on
EXPOSE 8080

# Start the application jar - this is not the uber jar used by the builder
# This jar only contains application code and references to the extracted jar files
# This layout is efficient to start up and AOT cache (and CDS) friendly
ENTRYPOINT ["java", "-jar", "application.jar"]