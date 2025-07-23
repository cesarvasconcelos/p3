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

@DisplayName( "Test class to demonstrate how to use magic url of testcontainers with a sliced @JdbcTest" )
@DataJpaTest
@ActiveProfiles( "test" ) // Activate the "test" profile, $mvn clean test -Dspring.profiles.active=test
// MAGIC URL OF TESTCONTAINERS: https://java.testcontainers.org/modules/databases/jdbc/
@TestPropertySource(properties = {
    "spring.test.database.replace=none",
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:tc:mysql:8.1:///db_bookstore"
})
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MagicURLBookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName( "Given books repository, when fetch all, then is empty" )
    void givenBookRepository_whenFindAll_thenIsEmpty()
    {
        assertThat( bookRepository.findAll() )
            .isEmpty();
    }
}