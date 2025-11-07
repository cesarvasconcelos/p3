package com.example.books;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test-specific configuration that provides a MySQL Testcontainer.
 *
 * Highlights:
 * - @TestConfiguration registers beans only for tests (not for production).
 * - @ServiceConnection lets Spring Boot auto-wire container connection details
 *   into the application's datasource (no manual URL/username/password needed).
 * - MySQLContainer is started lazily by tests when the datasource is required.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    /**
     * {@code @ServiceConnection}:
     * - Automatically configures the application to connect to this Testcontainer.
     * - Simplifies configuration: no need to set `spring.datasource.url`,
     *   `spring.datasource.username`, etc.
     * - Spring Boot detects the container and uses its connection properties
     *   for the datasource automatically.
     */
    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.1"));
        // .withDatabaseName("db_bookstore");
        // .withPassword("secret");
    }
}

/**
 * Default password behavior:
 * - If no custom password is set, the default user password 'test' is also
 *   applied to the MySQL root user.
 * - If you set a custom password (e.g., .withPassword("secret")), that value
 *   is used for both the database user and the MySQL root user.
 *
 * Examples:
 * - you can use both .withUsername("cesar").withPassword("cesar") -> creates user 'cesar' with password 'cesar'.
 * - or just use .withPassword("secret") -> sets the root user's password to 'secret' as well.
 */
