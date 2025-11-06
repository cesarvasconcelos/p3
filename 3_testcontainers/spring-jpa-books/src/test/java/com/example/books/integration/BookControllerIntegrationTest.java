package com.example.books.integration;

import com.example.books.TestcontainersConfiguration;
import com.example.books.controller.BookController;
import com.example.books.model.Book;
import com.example.books.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration tests for BookController endpoints.
 *
 * Key points:
 * - @SpringBootTest loads the full Spring context (not sliced tests).
 * - @AutoConfigureMockMvc provides MockMvc to test the MVC layer without starting a real server.
 * - @Import(TestcontainersConfiguration.class) brings in the MySQL Testcontainer bean for a real DB.
 * - @ActiveProfiles("test") activates the "test" profile (e.g., properties).
 * - @Transactional ensures each test runs in a transaction and rolls back after completion.
 * - @Sql sets up and tears down the DB schema/fixtures for deterministic tests.
 * - Uses the Arrange-Act-Assert (AAA) testing style for clarity.
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
    private MockMvc mockMvc; // Entry point to perform HTTP calls against the Spring MVC layer

    @Autowired
    private BookRepository bookRepository; // Direct repository access to verify database side-effects

    @Test
    // @WithMockUser: Simulates an authenticated user for security checks without hitting a real auth backend.
    // Skips checking the DB/user store; assumes "pedro" is authenticated with role ADMIN.
    @WithMockUser(username = "pedro", password = "abc", roles = { "ADMIN" }) // Simulate authenticated user
    @DisplayName("POST /books/add - Should persist book and redirect")
    void givenAuthenticatedUser_whenPostBook_thenItIsSavedAndRedirects() throws Exception {
        // Arrange: nothing to set up beyond the authentication provided by @WithMockUser.

        // Act: submit a form-like POST request to create a new book.
        mockMvc.perform(
                post("/books/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Mimics an HTML form submission
                        .param("title", "The Hobbit")
                        .param("price", "39.99")
                        .with(csrf()) // Include CSRF token (required when CSRF protection is enabled)
                // .with(user("pedro").password("abc").roles("ADMIN")) // Alternative explicit user; @WithMockUser already covers it
        )
        // Assert: controller redirects after successful creation
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/books"));

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
    void givenAuthenticatedUserAndExistingBook_whenDeleteById_thenItIsRemovedAndRedirects() throws Exception {
        // Arrange: create and persist a book to delete in this test
        Book book = new Book();
        book.setTitle("Test Book");
        book.setPrice(BigDecimal.valueOf(19.99));
        book = bookRepository.save(book); // Persist to DB and get the generated ID

        // Act: call the delete endpoint using the book's ID
        mockMvc.perform(get("/books/delete/{id}", book.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        // Assert: ensure the book has been removed from the database
        assertThat(bookRepository.findById(book.getId()))
                .as("Book should have been deleted from database but was not.")
                .isEmpty();
    }
}
