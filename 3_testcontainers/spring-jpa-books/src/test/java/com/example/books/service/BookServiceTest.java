package com.example.books.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.books.model.Book;
import com.example.books.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * Testes unitários puros para BookService — sem contexto Spring, sem banco de dados real (só um mock).
 *
 * Maven (via terminal):
 *   - Diretorio base: /Users/cesar/github/p3/3_testcontainers/spring-jpa-books
 *   - Rodar apenas esta classe:
 *       ./mvnw -Dtest=BookServiceTest test
 *   - Rodar um unico teste da classe:
 *       ./mvnw -Dtest=BookServiceTest#givenExistingBooks_whenFindAll_thenReturnsAllBooks test
 *
 * Principais escolhas de design:
 *   - Injeção manual via construtor, para que mudanças na assinatura
 *     do construtor gerem erro de compilação em vez de falha silenciosa de injeção.
 *   - Estilo BDDMockito (given/willReturn, then/should) para manter consistência com
 *     o restante da suíte de testes.
 *   - @ExtendWith(MockitoExtension.class) habilita STRICT_STUBS: qualquer stub definido
 *     e nunca usado durante o teste falha com UnnecessaryStubbingException — um recurso
 *     que detecta stubs desnecessários e mantém os testes enxutos.
 */
@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        // Espelha exatamente o que o Spring faz em tempo de execução: injeção por construtor.
        bookService = new BookService(bookRepository);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll returns every book the repository provides")
    void givenExistingBooks_whenFindAll_thenReturnsAllBooks() {
        // O sistema deve retornar todos os livros fornecidos pelo repositório ao buscar a lista completa.
        List<Book> books = List.of(aBook(), anotherBook());
        given(bookRepository.findAll()).willReturn(books);

        List<Book> result = bookService.findAll();

        assertThat(result).as("findAll should return exactly 2 books when 2 are available").hasSize(2);
        assertThat(result)
                .as("findAll should return exactly 2 books regardless of order")
                .containsExactlyInAnyOrder(aBook(), anotherBook());
        then(bookRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("findAll returns an empty list when the repository has no books")
    void givenNoBooks_whenFindAll_thenReturnsEmptyList() {
        // O sistema deve retornar uma lista vazia ao buscar todos os livros quando não houver registros no repositório.
        given(bookRepository.findAll()).willReturn(List.of());

        List<Book> result = bookService.findAll();

        assertThat(result).as("findAll should return an empty list when the repository has no books").isEmpty();
        then(bookRepository).should(times(1)).findAll();
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save delegates to the repository and returns the persisted book")
    void givenValidBook_whenSave_thenDelegatesAndReturnsSavedBook() {
        // O sistema deve delegar a persistência de um livro ao repositório e retornar o livro salvo.
        Book unsavedBook = aBook();
        given(bookRepository.save(unsavedBook)).willReturn(aSavedBook());

        Book result = bookService.save(unsavedBook);

        assertThat(result.getId()).as("saved book should have a database-assigned PK ID").isEqualTo(1L);
        assertThat(result.getTitle()).as("saved book title should match the original input").isEqualTo("Clean Code");
        assertThat(result.getPrice()).as("saved book price should match the original input").isEqualByComparingTo("39.99");
        then(bookRepository).should().save(unsavedBook);
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById delegates the deletion to the repository by ID")
    void givenValidId_whenDeleteById_thenDelegatesDeleteToRepository() {
        // O sistema deve delegar ao repositório a exclusão de um livro com base no seu ID.
        // willDoNothing() tecnicamente é um no-op para métodos void mockados (comportamento padrão),
        // mas deixa a intenção explícita e ensina o padrão de stubbing para métodos void.
        willDoNothing().given(bookRepository).deleteById(1L);

        bookService.deleteById(1L);

        then(bookRepository).should().deleteById(1L);
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById returns a non-empty Optional when the book exists")
    void givenExistingId_whenFindById_thenReturnsPresentOptional() {
        // O sistema deve retornar um Optional contendo o livro ao buscar por um ID existente.
        given(bookRepository.findById(1L)).willReturn(Optional.of(aBook()));

        Optional<Book> result = bookService.findById(1L);

        assertThat(result).as("findById should return a non-empty Optional when the book exists").isPresent();
        assertThat(result.get().getTitle()).as("returned book title should match the stored value").isEqualTo("Clean Code");
        then(bookRepository).should().findById(1L);
    }

    @Test
    @DisplayName("findById returns an empty Optional when no book matches the ID")
    void givenNonExistingId_whenFindById_thenReturnsEmptyOptional() {
        // O sistema deve retornar um Optional vazio ao buscar um livro por ID inexistente.
        given(bookRepository.findById(99L)).willReturn(Optional.empty());

        Optional<Book> result = bookService.findById(99L);

        assertThat(result).as("findById should return an empty Optional when no book matches the ID").isEmpty();
        then(bookRepository).should().findById(99L);
    }

    // ── updateBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBook mutates the managed entity's fields without calling save explicitly")
    void givenExistingBook_whenUpdateBook_thenAppliesNewFieldsToManagedEntity() {
        // O sistema deve atualizar os campos de um livro gerenciado sem necessidade de chamada explícita ao método de salvar.
        // O serviço altera a entidade retornada por findById in-place e depende do
        // dirty-checking do Hibernate para persistir — sem chamada explícita a save().
        Book managedBook = aBook();
        Book updateData = aBookWithNewData();
        given(bookRepository.findById(1L)).willReturn(Optional.of(managedBook));

        bookService.updateBook(1L, updateData);

        assertThat(managedBook.getTitle()).as("updateBook should overwrite the managed entity's title in-place").isEqualTo("Effective Java");
        assertThat(managedBook.getPrice()).as("updateBook should overwrite the managed entity's price in-place").isEqualByComparingTo("49.99");
        // Sem save explícito — o dirty-checking cuida da persistência no commit da transação.
        then(bookRepository).should(never()).save(managedBook);
    }

    @Test
    @DisplayName("updateBook does nothing beyond findById when the book is not found")
    void givenNonExistingBook_whenUpdateBook_thenNoFurtherRepositoryInteractions() {
        // O sistema não deve realizar nenhuma alteração ao tentar atualizar um livro inexistente, limitando-se à busca por ID.
        given(bookRepository.findById(99L)).willReturn(Optional.empty());

        bookService.updateBook(99L, aBookWithNewData());

        then(bookRepository).should().findById(99L);
        then(bookRepository).shouldHaveNoMoreInteractions();
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private static Book aBook() {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setPrice(new BigDecimal("39.99"));
        return book;
    }

    private static Book aSavedBook() {
        Book book = aBook();
        book.setId(1L); // aSavedBook receberá uma PK do banco de dados
        return book;
    }

    private static Book anotherBook() {
        Book book = new Book();
        book.setTitle("The Pragmatic Programmer");
        book.setPrice(new BigDecimal("44.99"));
        return book;
    }

    private static Book aBookWithNewData() {
        Book book = new Book();
        book.setTitle("Effective Java");
        book.setPrice(new BigDecimal("49.99"));
        return book;
    }
}
