package com.example.books;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration( proxyBeanMethods = false )
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection // Automatically configures the application to connect to this container
    // Simplifies Configuration: Automatically connects the Spring Boot application to the test
    // container without manually setting properties like spring.datasource.url,
    // spring.datasource.username, etc.
    // No values are explicitly necessary in the application.properties file for connecting to this test container
    // Spring Boot will automatically detect the container and use its connection properties
    // @ServiceConnection instructs Spring Boot to use the container for configuring the datasource automatically.
    MySQLContainer<?> mysqlContainer()
    {
        return new MySQLContainer<>( DockerImageName.parse( "mysql:8.1" ) );
            // .withDatabaseName( "db_bookstore" );
            // .withPassword( "secret" );
    }
}

/**
 * If no custom password is specified, the container will use the default user password 'test' for the root user as well.
 * When you specify a custom password for the database user, this will also act as the password of the MariaDB root user automatically.
 * Ou seja, ou pode-se usar .withUsername( "cesar" ) e .withPassword( "cesar" ) se quiser um user específico com senha,
 * ou apenas especificar o .withPassword( "secret" ) e deixar que user root já pegue esta senha secret
 */
