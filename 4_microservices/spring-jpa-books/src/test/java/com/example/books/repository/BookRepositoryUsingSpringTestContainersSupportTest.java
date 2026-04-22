package com.example.books.repository;

import com.example.books.TestcontainersConfiguration;
import com.example.books.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-focused tests using the sliced {@code @DataJpaTest} configuration and Testcontainers.
 *
 * How to run from the module directory (3_testcontainers/spring-jpa-books):
 * <pre>
 *   # Run the entire test class
 *   ./mvnw -Dspring.profiles.active=test \
 *     -Dtest=com.example.books.repository.BookRepositoryUsingSpringTestContainersSupportTest \
 *     test
 *
 *   # Run a single test method
 *   ./mvnw -Dspring.profiles.active=test \
 *     -Dtest="com.example.books.repository.BookRepositoryUsingSpringTestContainersSupportTest#givenBook_whenSave_thenBookIsPersisted" \
 *     test
 * </pre>
 *
 * Highlights:
 * - {@code @DataJpaTest} boots only JPA-related components (repositories, entities, JPA config) for fast tests.
 * - {@code @Import(TestcontainersConfiguration.class)} wires a MySQL Testcontainer as the datasource.
 * - {@code @ActiveProfiles("test")} activates the "test" profile. You can also pass
 *   {@code -Dspring.profiles.active=test} when running tests (e.g., {@code mvn clean test -Dspring.profiles.active=test}).
 * - {@code @Sql} sets up and tears down the schema and data around each test for repeatability.
 *
 * Why use both {@code TestEntityManager} and the repository?
 * - Tests aim to verify repository behavior. To avoid mixing responsibilities, use {@code TestEntityManager}
 *   for preparing database state (Arrange), then call repository methods (Act), and finally assert outcomes (Assert).
 *   This ensures that what you validate is strictly the repository method under test, and not setup logic.
 *
 * Why call {@code entityManager.clear()} before Act or Assert?
 * - After {@code persist()} or {@code save()}, entities are held in the JPA first-level (L1) cache.
 * - Reads that follow — even JPQL queries — may return the cached in-memory instance instead of
 *   fetching from the database, producing false positives.
 * - {@code clear()} evicts all managed entities from the persistence context, forcing the next read
 *   to execute a real database query. This is essential to verify that the write actually reached the DB.
 */
@DisplayName("Test class for BookRepository CRUD Operations with Testcontainers and sliced @DataJpaTest")
@DataJpaTest // Enables Spring Data JPA testing and rolls back transactions after each test
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Do not use an embedded database (H2, HSQLDB, Derby) if it's on the classpath
@Import({TestcontainersConfiguration.class})
@ActiveProfiles("test")
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookRepositoryUsingSpringTestContainersSupportTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Given an empty repository, when fetching all books, then return empty set")
    void givenEmptyBookRepo_whenFindAll_thenReturnEmpty() {
        // Act
        List<Book> books = bookRepository.findAll();

        // Assert
        assertThat(books)
                .as("Database should not contain pre-loaded data from migration scripts")
                .isEmpty();
    }

    @Test
    @DisplayName("Given a set of books, when fetching all, then return all books")
    void givenSetOfBooks_whenFindAll_thenReturnAllBooks() {
        // Arrange
        persistBook("Book 1", 19.99);
        persistBook("Book 2", 29.99);
        entityManager.clear(); // evict cache so findAll() hits the database

        // Act
        List<Book> books = bookRepository.findAll();

        // Assert
        assertThat(books)
                .as("#findAll() should return exactly two books")
                .hasSize(2);
        assertThat(books)
                .as("#findAll() should preserve insertion order when JPA returns entities")
                .extracting(Book::getTitle)
                .containsExactly("Book 1", "Book 2");
    }

    @Test
    @DisplayName("Given a book, when saving, then book is persisted in the database")
    void givenBook_whenSave_thenBookIsPersisted() {
        // Arrange
        Book book = aBook("New Book", 39.99);

        // Act
        bookRepository.save(book);
        entityManager.clear(); // evict cache so the next read hits the database

        // Assert
        List<Book> books = findAllBooks();
        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getTitle())
                .as("Book title should be equal to 'New Book'")
                .isEqualTo("New Book");
        assertThat(books.getFirst().getPrice())
                .as("Book price should be equal to 39.99")
                .isEqualTo(BigDecimal.valueOf(39.99));
    }

    @Test
    @DisplayName("Given an existing book, when deleting, then book is removed from the database")
    void givenExistingBook_whenDelete_thenBookIsRemoved() {
        // Arrange
        persistBook("Book 1", 19.99);
        entityManager.clear(); // evict cache before reading to verify setup

        List<Book> books = findAllBooks();
        assertThat(books).hasSize(1);

        // Act
        bookRepository.delete(books.getFirst());

        // Assert
        assertThat(findAllBooks())
                .as("The deleted book should have been removed")
                .isEmpty();
    }

    @Test
    @DisplayName("Given an existing book, when updating its data, then book data is changed in the database")
    void givenExistingBook_whenUpdate_thenBookIsUpdated() {
        // Arrange
        persistBook("Book 1", 19.99);
        entityManager.clear(); // evict cache so we read the entity from the database

        Book book = findAllBooks().getFirst();
        book.setTitle("Book 2");
        book.setPrice(BigDecimal.valueOf(29.99D));

        // Act
        bookRepository.saveAndFlush(book);
        entityManager.clear(); // evict cache so the next read hits the database

        // Assert
        List<Book> books = findAllBooks();
        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getTitle())
                .as("Book title should be equal to 'Book 2'")
                .isEqualTo("Book 2");
        assertThat(books.getFirst().getPrice())
                .as("Book price should be equal to 29.99")
                .isEqualTo(BigDecimal.valueOf(29.99));
    }

    @Test
    @DisplayName("Given a valid book Id, when finding by Id, then return book")
    void givenValidBookId_whenFindById_thenReturnBook() {
        // Arrange
        persistBook("Book 1", 19.99);
        Long bookId = findAllBooks().getFirst().getId();
        entityManager.clear(); // evict cache so findById() hits the database

        // Act
        Optional<Book> book = bookRepository.findById(bookId);

        // Assert
        assertThat(book.isPresent())
                .as("A book should be returned for a valid Id")
                .isTrue();
    }

    @Test
    @DisplayName("Given an invalid book Id, when finding by Id, then return empty")
    void givenInvalidBookId_whenFindById_thenReturnEmpty() {
        // Act
        Optional<Book> bookDoesNotExist = bookRepository.findById(-1L);

        // Assert
        assertThat(bookDoesNotExist)
                .as("No books should be returned for an invalid Id")
                .isEmpty();
    }

    // --- Helpers ---

    /**
     * Factory: builds a transient Book without an id.
     * Keeps constructor details out of each test.
     */
    private Book aBook(String title, double price) {
        return new Book(null, title, price);
    }

    /**
     * Arrange helper: persist a book and flush to the database.
     * Use this during Arrange to prepare DB state without calling repository methods.
     */
    private void persistBook(String title, double price) {
        entityManager.persistAndFlush(aBook(title, price));
    }

    /**
     * Assert helper: query all books via JPQL to verify persisted state.
     * Call after {@code entityManager.clear()} to ensure results come from the database.
     */
    private List<Book> findAllBooks() {
        return entityManager.getEntityManager()
                .createQuery("SELECT b FROM Book b", Book.class)
                .getResultList();
    }
}
