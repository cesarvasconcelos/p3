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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName( "Test class for BookController endpoints - full integration tests" )
@AutoConfigureMockMvc
@Import({ TestcontainersConfiguration.class })
@ActiveProfiles("test")
@Transactional // ensures each test rolls back after running
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Test
    // @WithMockUser: Pretend this request is coming from a logged-in user with these credentials and roles — don’t check if they really exist.
    // Skip checking the DB. Assume someone named pedro is already logged in with role ADMIN.”
    @WithMockUser( username="pedro", password="abc", roles={"ADMIN"} )
    @DisplayName("POST /books/add - Should persist book and make it the only entry in DB")
    void testAddBookPersistsCorrectly() throws Exception {
        // Testing performing POST to add a book
        mockMvc.perform(post("/books/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED) // mimics an HTML form submission.
                        .param("title", "The Hobbit")
                        .param("price", "39.99")
                        .with(csrf())) // Add CSRF token
                        //.with(user("pedro").password( "abc" ).roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        // Then: Check repository
        assertThat(bookRepository.count()).isEqualTo(1);

        Book book = bookRepository.findAll().getFirst();
        assertThat(book.getTitle()).isEqualTo("The Hobbit");
        assertThat(book.getPrice().doubleValue()).isEqualTo(39.99D);
    }
}
