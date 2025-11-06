package com.example.books.repository;

import com.example.books.TestcontainersConfiguration;
import com.example.books.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
 */
@DisplayName("Test class for BookRepository CRUD Operations with Testcontainers and sliced @DataJpaTest")
@DataJpaTest // Enables Spring Data JPA testing and rolls back transactions after each test
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
    void givenEmptyBookRepo_whenFindingAll_thenIsEmpty() {
        // Act
        List<Book> books = bookRepository.findAll();

        // Assert
        assertThat(books)
                .as("Database should not contain pre-loaded data from migration scripts")
                .isEmpty();
    }

    @Test
    @DisplayName("Given a set of books, when fetching all, then return all books")
    void givenSetOfBooks_whenFindingAll_thenReturnAllBooks() {
        // Arrange: insert two books using TestEntityManager (setup)
        Book book = new Book(null, "Book 1", 19.99);
        entityManager.persist(book);
        entityManager.flush();

        insertBookIntoDatabase("Book 2", 29.99);

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
    @DisplayName("Given a book data, when saving at repository, then success")
    void givenBook_whenSaving_thenSuccess() {
        // Arrange
        Book book = new Book(null, "New Book", 39.99);

        // Act
        bookRepository.save(book);

        // Assert
        List<Book> books = selectAllBooksFromDatabase();
        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getTitle())
                .as("Book title should be equal to 'New Book'")
                .isEqualTo("New Book");
        assertThat(books.getFirst().getPrice())
                .as("Book price should be equal to 39.99")
                .isEqualTo(BigDecimal.valueOf(39.99));
    }

    @Test
    @DisplayName("Given an existing book, when deleting by id, then success")
    void givenBook_whenDeleting_thenSuccess() {
        // Arrange
        insertBookIntoDatabase("Book 1", 19.99);
        List<Book> books = selectAllBooksFromDatabase();
        assertThat(books).hasSize(1);

        // Act
        Book first = books.getFirst();
        bookRepository.delete(first);

        // Assert
        books = selectAllBooksFromDatabase();
        assertThat(books)
                .as("The deleted book should have been removed")
                .isEmpty();
    }

    @Test
    @DisplayName("Given an existing book, when updating its data, then success")
    void givenBook_whenUpdating_thenSuccess() {
        // Arrange
        insertBookIntoDatabase("Book 1", 19.99);
        List<Book> books = selectAllBooksFromDatabase();

        Book book = books.getFirst();
        book.setTitle("Book 2");
        book.setPrice(BigDecimal.valueOf(29.99D));

        // Act
        bookRepository.save(book);

        // Assert
        books = selectAllBooksFromDatabase();
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
    public void givenValidBookId_whenFindById_thenReturnBook() {
        // Arrange
        insertBookIntoDatabase("Book 1", 19.99);
        List<Book> books = selectAllBooksFromDatabase();
        Long bookId = books.getFirst().getId();

        // Act
        Optional<Book> book = bookRepository.findById(bookId);

        // Assert
        assertThat(book.isPresent())
                .as("A book should be returned for a valid Id")
                .isTrue();
    }

    @Test
    @DisplayName("Given an invalid book Id, when finding by Id, then return empty")
    public void givenInvalidBookId_whenFindById_thenReturnEmpty() {
        // Act
        Optional<Book> bookDoesNotExist = bookRepository.findById(-1L);

        // Assert
        assertThat(bookDoesNotExist)
                .as("No books should be returned for an invalid Id")
                .isEmpty();
    }

    /**
     * Helper: insert one book using the TestEntityManager to prepare DB state.
     * Prefer this TestEntityManager over calling repository methods during Arrange to avoid mixing concerns.
     */
    private void insertBookIntoDatabase(String bookTitle, double bookPrice) {
        Book book = new Book(null, bookTitle, bookPrice);
        entityManager.persist(book);
        entityManager.flush();
    }

    /**
     * Helper: select all books directly via JPQL to verify persisted state.
     */
    private List<Book> selectAllBooksFromDatabase() {
        return entityManager.getEntityManager()
                .createQuery("SELECT b FROM Book b", Book.class)
                .getResultList();
    }
}
