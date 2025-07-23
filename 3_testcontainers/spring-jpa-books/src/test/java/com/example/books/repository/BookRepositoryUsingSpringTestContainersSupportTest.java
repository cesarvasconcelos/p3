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

/*
    Quando se usa as classes de suporte do Spring inititizr para Testcontainers,
    as classes de testes podem apenas fazer @Import(TestConfiguration.class) para
    se beneficiar imediatamente do container integrado ao Junit <--- é o caso deste exemplo
 */
@DisplayName("Test class for repositories using TestContainers and sliced @DataJpaTest")
@DataJpaTest // Enables Spring Data JPA testing (rolls back transactions after each test)
@Import({TestcontainersConfiguration.class})
@ActiveProfiles("test") // Activate the "test" profile, $mvn clean test -Dspring.profiles.active=test
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookRepositoryUsingSpringTestContainersSupportTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    /*
       Observação importante:
       Note que além do TestEntityManager, estou injetando meu bookRepository.
       Isso porque, nos @Test methods, meu interesse é testar os métodos de bookRepository.
       Por isso, para não misturar o escopo do test, vou usar TestEntityManager sempre que precisar
       preparar o database, de forma tal que eu observe o comportamento APENAS do bookRepository.method() que preciso testar.
       Se não fizer isso, acabarei tendo de chamar outros métodos de bookRepository
       para verificar o resultado final ou preparar o db antes de testar --o que acaba misturando os papéis.
    */

    @Test
    @DisplayName("Given an empty repository, when fetching all books, then return empty set")
    void givenEmptyBookRepo_whenFindingAll_thenIsEmpty() {
        assertThat(bookRepository.findAll())
            .as("Database should not contain books pre-loaded data from Flyway scripts")
            .isEmpty();
    }

    @Test
    @DisplayName("Given a set of books, when fetching all, then return all books")
    void givenSetOfBooks_whenFindingAll_thenReturnAllBooks() {
        // pode fazer manualmente a criação do livro e inserção ou criar uma helper function
        Book book = new Book(null, "Book 1", 19.99 );
        entityManager.persist(book);
        entityManager.flush();
        //insertBookIntoDatabase("Book 1", 19.99);
        insertBookIntoDatabase("Book 2", 29.99);

        List<Book> books = bookRepository.findAll();

        assertThat(books)
            .as("#findAll() should return only two books")
            .hasSize(2);
        assertThat(books)
            .as("#findAll() should return only two books")
            .extracting(Book::getTitle)
            .containsExactly("Book 1", "Book 2");
    }

    @Test
    @DisplayName("Given a book data, when saving at repository, then success")
    void givenBook_whenSaving_thenSuccess() {
        Book book = new Book(null, "New Book", 39.99);

        bookRepository.save(book);

        List<Book> books = selectAllBooksFromDatabase();

        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getTitle())
            .as("Book title should be equals to 'New Book'")
            .isEqualTo("New Book");
        assertThat(books.getFirst().getPrice())
            .as("Book price should be equals to 39.99")
            .isEqualTo(BigDecimal.valueOf(39.99));
    }

    @Test
    @DisplayName("Given an existing book, when deleting by id, then success")
    void givenBook_whenDeleting_thenSuccess() {
        insertBookIntoDatabase("Book 1", 19.99);
        List<Book> books = selectAllBooksFromDatabase();
        assertThat(books).hasSize(1);

        Book first = books.getFirst();
        bookRepository.delete(first);

        books = selectAllBooksFromDatabase();
        assertThat(books)
            .as("The deleted book should have been removed")
            .isEmpty();
    }

    @Test
    @DisplayName("Given an existing book, when updating its data, then success")
    void givenBook_whenUpdating_thenSuccess() {
        insertBookIntoDatabase("Book 1", 19.99);
        List<Book> books = selectAllBooksFromDatabase();

        Book book = books.getFirst();
        book.setTitle("Book 2");
        book.setPrice(BigDecimal.valueOf(29.99D));

        bookRepository.save(book);

        books = selectAllBooksFromDatabase();

        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getTitle())
            .as("Book title should be equal to 'Book 2'")
            .isEqualTo("Book 2");
        assertThat(books.getFirst().getPrice())
            .as("Book price should be equals to 29.99")
            .isEqualTo(BigDecimal.valueOf(29.99));
    }

    @Test
    @DisplayName("Given a valid book Id, when finding by Id, then return book")
    public void givenValidBookId_whenFindById_thenReturnBook() {
        insertBookIntoDatabase("Book 1", 19.99);
        List<Book> books = selectAllBooksFromDatabase();
        Long bookId = books.getFirst().getId();

        Optional<Book> book = bookRepository.findById(bookId);

        assertThat(book.isPresent())
            .as("A book should have been returned from a valid Id")
            .isTrue();
    }

    @Test
    @DisplayName("Given an invalid book Id, when finding by Id, then return empty")
    public void givenInvalidBookId_whenFindById_thenReturnEmpty() {
        Optional<Book> bookDoesNotExist = bookRepository.findById(-1L);

        assertThat(bookDoesNotExist)
            .as("No books should have been returned")
            .isEmpty();
    }

    // Helper methods
    private void insertBookIntoDatabase(String bookTitle, double bookPrice) {
        Book book = new Book(null, bookTitle, bookPrice);
        entityManager.persist(book);
        entityManager.flush();
    }

    private List<Book> selectAllBooksFromDatabase() {
        return entityManager.getEntityManager()
                            .createQuery("SELECT b FROM Book b", Book.class)
                            .getResultList();
    }
}
