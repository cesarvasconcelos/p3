package com.example.books;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * Test-specific configuration that provides a MariaDB Testcontainer.
 *
 * Highlights:
 * - @TestConfiguration registers beans only for tests (not for production).
 * - @ServiceConnection lets Spring Boot auto-wire container connection details
 *   into the application's datasource (no manual URL/username/password needed).
 * - MariaDBContainer is started lazily by tests when the datasource is required.
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
     *
     * - From Magnus Larsson book (https://a.co/d/cpr3FGn): 'The @ServiceConnection annotation will auto-configure the
     *   corresponding Spring connection properties. It will, for example, set the
     *   spring.datasource.url, spring.datasource.username, and spring.datasource.password
     *   properties based on the information from the database container using the getJdbcUrl(),
     *   getUsername(), and getPassword() methods'
     */
    @Bean
    @ServiceConnection
    MariaDBContainer mariaDbContainer() {
        return new MariaDBContainer( DockerImageName.parse( "mariadb:lts" ))
                // Keeps MariaDB data in RAM (tmpfs): avoids slow disk fsync during initialization.
                // Test data is disposable anyway, so there is no need to persist it on disk.
                .withTmpFs( Map.of( "/var/lib/mysql", "rw" ));
        // .withDatabaseName("db_bookstore");
        // .withPassword("secret");
    }
}

/**
 * Default password behavior:
 * - If no custom password is set, the default user password 'test' is also
 *   applied to the MariaDB root user.
 * - If you set a custom password (e.g., .withPassword("secret")), that value
 *   is used for both the database user and the MariaDB root user.
 *
 * Examples:
 * - you can use both .withUsername("cesar").withPassword("cesar") -> creates user 'cesar' with password 'cesar'.
 * - or just use .withPassword("secret") -> sets the root user's password to 'secret' as well.
 */
