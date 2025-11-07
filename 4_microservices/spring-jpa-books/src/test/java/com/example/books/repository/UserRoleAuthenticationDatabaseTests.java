package com.example.books.repository;

import com.example.books.TestcontainersConfiguration;
import com.example.books.config.PasswordEncoderConfig;
import com.example.books.model.Role;
import com.example.books.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Use real database (Testcontainers)
@DisplayName( "Test class User/Role Repository with Authentication using a sliced @DataJpaTest" )
@DataJpaTest // Enables Spring Data JPA testing (rolls back transactions after each test)
@Import({PasswordEncoderConfig.class, TestcontainersConfiguration.class})
@ActiveProfiles("test") // Activate the "test" profile, $mvn clean test -Dspring.profiles.active=test
@Sql(scripts = "classpath:/sql/create-test-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:/sql/drop-test-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserRoleAuthenticationDatabaseTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Given user with role, when fetching from db, then success")
    void givenUserWithRole_whenFetchingFromDb_thenSuccess() {
        // Given: Save a user with a role
        User user = new User("David", "12345");
        Role role = new Role("student");
        user.setRole(role);

        userRepository.save(user);

        // When: Fetch the user with the role
        Optional<User> userWithRole = userRepository.findUserWithRoleByName("David");

        // Then: Verify the result
        assertThat(userWithRole)
            .as("Fetched user from database should exist but it doesn't")
            .isPresent();

        assertThat(userWithRole.get())
            .as("Fetched user from database should be equal to original saved user")
            .isEqualTo(user);

        assertThat(userWithRole.get().getName())
            .as("Fetched user from database should have name 'David' but it's not")
            .isEqualTo("David");

        Role student = userWithRole.get().getRole();
        assertThat(student)
            .as("Fetched role's user from database should be equal to original saved role")
            .isEqualTo(role);

        assertThat(student.getRole())
            .as("Fetched role's user from database should be 'student' but it's not")
            .isEqualTo("student");
    }

    @Test
    @DisplayName("Given user with hashed password, when fetching from db, then password matches")
    void givenUserWithPasswordHashed_whenFetchingFromDb_thenPasswordMatches() {
        // Given: Save a user with a role and a hashed password
        String password = "12345";
        User user = new User("David", passwordEncoder.encode(password));
        Role role = new Role("student");
        user.setRole(role);

        userRepository.save(user);

        // When: Fetch the user with the role
        Optional<User> fetchedUser = userRepository.findUserWithRoleByName("David");

        // Then: Verify that the original and hashed passwords match
        assertThat(fetchedUser)
            .as("Fetched user from database should exist but it doesn't")
            .isPresent();

        assertThat(passwordEncoder.matches(password, fetchedUser.get().getPassword()))
            .as("Stored hashed password should match the original password")
            .isTrue();
    }
}
