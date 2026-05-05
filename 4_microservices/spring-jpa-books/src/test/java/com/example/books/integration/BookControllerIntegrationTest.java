package com.example.books.integration;

import com.example.books.TestcontainersConfiguration;
import com.example.books.model.Book;
import com.example.books.repository.BookRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Full integration tests for BookController endpoints.
 *
 * Key points:
 * - @SpringBootTest loads the full Spring context (not sliced tests).
 * - @AutoConfigureMockMvc provides MockMvcTester to test the MVC layer without starting a real server.
 * - @Import(TestcontainersConfiguration.class) brings in the MySQL Testcontainer bean for a real DB.
 * - @ActiveProfiles("test") activates the "test" profile (e.g., properties).
 * - @Transactional ensures each test runs in a transaction and rolls back after completion.
 * - @Sql sets up and tears down the DB schema/fixtures for deterministic tests.
 * - Uses the Arrange-Act-Assert (AAA) testing style for clarity.
 * - MockMvcTester (Spring 6.2+) replaces MockMvc so that ALL assertions use AssertJ exclusively,
 *   eliminating the mixed assertion style and the need for "throws Exception" on every test method.
 *
 * Maven commands (educational):
 * - Run all tests with the test profile:
 *   mvn clean test -Dspring.profiles.active=test
 * - Run one test class with the test profile:
 *   mvn -Dtest=BookControllerIntegrationTest test -Dspring.profiles.active=test
 * - Run one specific test method with the test profile:
 *   mvn -Dtest=BookControllerIntegrationTest#givenAuthenticatedUser_whenPostBook_thenItIsSavedAndRedirects test -Dspring.profiles.active=test
 */
@SpringBootTest
@DisplayName("Test class for BookController endpoints - full integration tests")
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class }) // Wires in the MySQL Testcontainer as a datasource
@ActiveProfiles("test")
@Transactional // Ensures each test rolls back after running (isolated tests)
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Prepare schema/data
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)  // Clean up schema/data
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc; // AssertJ-based entry point; auto-registered by @AutoConfigureMockMvc

    @Autowired
    private BookRepository bookRepository; // Direct repository access to verify database side-effects

    @Test
    // @WithMockUser: Simulates an authenticated user for security checks without hitting a real auth backend.
    // Skips checking the DB/user store; assumes "pedro" is authenticated with role ADMIN.
    @WithMockUser(username = "pedro", password = "abc", roles = { "ADMIN" }) // Simulate authenticated user
    @DisplayName("POST /books/add - Should persist book and redirect")
    void givenAuthenticatedUser_whenPostBook_thenItIsSavedAndRedirects() {
        // Arrange: nothing to set up beyond the authentication provided by @WithMockUser.

        // Act: submit a form-like POST request to create a new book.
        // perform() returns MvcTestResult — no checked exception, no "throws Exception" needed.
        var response = mockMvc.perform(
                post("/books/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Mimics an HTML form submission
                        .param("title", "The Hobbit")
                        .param("price", "39.99")
                        .with(csrf()) // Include CSRF token (required when CSRF protection is enabled)
        );

        // Assert: HTTP layer — controller redirects after successful creation.
        // Both assertions use AssertJ, the same library used below for business assertions.
        assertThat(response)
                .as("Expected a 302 redirect after creating a book.")
                .hasStatus(HttpStatus.FOUND);

        assertThat(response)
                .redirectedUrl()
                .as("Expected redirect to /books after creation.")
                .isEqualTo("/books");

        // Assert: verify the book was persisted in the database
        assertThat(bookRepository.count())
                .as("Expected exactly one book to be saved, but found a different count.")
                .isEqualTo(1);

        Book book = bookRepository.findAll().getFirst();
        assertThat(book.getTitle())
                .as("Expected the saved book to have title 'The Hobbit', but it did not.")
                .isEqualTo("The Hobbit");

        assertThat(book.getPrice().doubleValue())
                .as("Expected the saved book to have price $39.99, but it did not.")
                .isEqualTo(39.99D);
    }

    @Test
    @WithMockUser(username = "pedro", password = "abc", roles = { "ADMIN" }) // Simulate authenticated user
    @DisplayName("GET /books/delete/{id} - Should delete book and redirect")
    void givenAuthenticatedUserAndExistingBook_whenDeleteById_thenItIsRemovedAndRedirects() {
        // Arrange: create and persist a book to delete in this test
        Book book = aBook("Test Book", 19.99);
        Book saved = persistBook( book ); // Persist to DB and get the generated ID

        // Act: call the delete endpoint using the book's ID
        var response = mockMvc.perform(get("/books/delete/{id}", saved.getId()));

        // Assert: HTTP layer — controller redirects after deletion
        assertThat(response)
                .as("Expected a 302 redirect after deleting a book.")
                .hasStatus(HttpStatus.FOUND);

        assertThat(response)
                .redirectedUrl()
                .as("Expected redirect to /books after deletion.")
                .isEqualTo("/books");

        // Assert: ensure the book has been removed from the database
        assertThat(bookRepository.findById(book.getId()))
                .as("Book should have been deleted from database but was not.")
                .isEmpty();
    }

    @Test
    @WithMockUser(username = "pedro", password = "abc", roles = { "ADMIN" }) // Simulate authenticated user
    @DisplayName("POST /books/edit/{id} - Should update book data and redirect")
    void givenAuthenticatedUserAndExistingBook_whenEdit_thenItIsUpdatedAndRedirects(){
        // Arrange: create and persist a book to be edited in this test
        Book book = aBook( "Old Book", 19.99 );
        Book saved = persistBook( book ); // Persist to DB and get the generated ID

        // Act: submit a form-like POST request to update the book's data.
        // perform() returns MvcTestResult — no checked exception, no "throws Exception" needed.
        var response = mockMvc.perform(
                post("/books/edit/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Mimics an HTML form submission
                        .param("title", "New Book")
                        .param("price", "39.99")
                        .with(csrf()) // Include CSRF token (required when CSRF protection is enabled)
        );

        // Assert: HTTP layer — controller redirects after successful update.
        // Both assertions use AssertJ, the same library used below for business assertions.
        assertThat(response)
                .as("Expected a 302 redirect after update.")
                .hasStatus(HttpStatus.FOUND);

        assertThat(response)
                .redirectedUrl()
                .as("Expected redirect to /books after update.")
                .isEqualTo("/books");

        // Assert: verify the book's data was updated in the database
        assertThat(bookRepository.count())
                .as("Expected exactly one book to be updated, but found a different count.")
                .isEqualTo(1);

        book = bookRepository.findAll().getFirst();
        assertThat(book.getTitle())
                .as("Expected the updated book to have title 'New Book', but it did not.")
                .isEqualTo("New Book");

        assertThat(book.getPrice().doubleValue())
                .as("Expected the updated book to have price $39.99, but it did not.")
                .isEqualTo(39.99D);
    }

    // Helper methods
    private Book persistBook( Book book ){
        return bookRepository.saveAndFlush( book );
    }

    private Book aBook(String title, double price){
        Book book = new Book();
        book.setTitle(title);
        book.setPrice(BigDecimal.valueOf(price));
        return book;
    }
}
