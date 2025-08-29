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

@DisplayName( "Test class for BookRepository CRUD Operations with Magic Url and sliced @DataJpaTest" )
@DataJpaTest
@ActiveProfiles( "test" ) // Activate the "test" profile, $mvn clean test -Dspring.profiles.active=test
// MAGIC URL OF TESTCONTAINERS: https://java.testcontainers.org/modules/databases/jdbc/
@TestPropertySource(properties = {
    // "spring.test.database.replace=none",
    // "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:tc:mysql:8.1:///mytestdb"
    // se tiver um init script: jdbc:tc:mysql:8.0.36:///databasename?TC_INITSCRIPT=somepath/init_mysql.sql
})
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MagicURLBookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Given an empty repository, when fetching all books, then return empty set")
    void givenEmptyBookRepo_whenFindingAll_thenIsEmpty() {
        assertThat(bookRepository.findAll())
            .as("Database should not contain any books pre-loaded data from Flyway scripts")
            .isEmpty();
    }
}