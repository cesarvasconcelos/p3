package com.example.books.repository;

import com.example.books.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests using sliced {@code @DataJpaTest} with the Testcontainers JDBC "magic URL".
 *
 * Highlights:
 * - {@code @DataJpaTest}: boots only JPA components for fast, isolated tests; each test rolls back.
 * - {@code @ActiveProfiles("test")}: activates the test profile for consistent test configuration.
 * - {@code @TestPropertySource}: sets the datasource URL to the Testcontainers magic URL
 *   {@code jdbc:tc:mysql:8.1:///mytestdb}, which automatically starts a MySQL container
 *   and wires a datasource without manual properties.
 * - {@code @Sql}: prepares and cleans the schema/data around each test for determinism.
 *
 * Reference: https://java.testcontainers.org/modules/databases/jdbc/
 */
@DisplayName("Test class for BookRepository CRUD Operations with Magic Url and sliced @DataJpaTest")
@DataJpaTest
@ActiveProfiles( "test" ) // Activate the "test" profile, $mvn clean test -Dspring.profiles.active=test
// MAGIC URL OF TESTCONTAINERS: https://java.testcontainers.org/modules/databases/jdbc/
@TestPropertySource(properties = {
        // "spring.test.database.replace=none",
        // "spring.flyway.enabled=false",
        "spring.datasource.url=jdbc:tc:mysql:8.1:///mytestdb"
        // If you have an init script:
        // "spring.datasource.url=jdbc:tc:mysql:8.0.36:///databasename?TC_INITSCRIPT=classpath:/sql/init_mysql.sql"
})
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MagicURLBookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Given an empty repository, when fetching all books, then return empty set")
    void givenEmptyBookRepo_whenFindingAll_thenIsEmpty() {
        // Act
        var books = bookRepository.findAll();

        // Assert
        assertThat(books)
                .as("Database should not contain any books pre-loaded from Flyway/migration scripts")
                .isEmpty();
    }
}