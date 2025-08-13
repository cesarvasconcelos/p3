## How to run the app during development stage:

- `mvn clean package -DskipTests` to **package** and create a Jar file **without** running tests
- `mvn spring-boot:start` **to run** the app using Jar file
- `mvn spring-boot:stop` **to stop** the app using Jar file

## New changes from earlier Spring Books Application (without authentication):

- In `pom.xml`, add the following dependencies:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Database changes:
- Aside from previous `tbl_book` table, now create the **User** (`tbl_user`) and **Role** (`tbl_role`)
  tables and insert some sample data:

```sql
-- ------------------------------------------------
-- Create tables
-- ------------------------------------------------
CREATE TABLE tbl_book
(
    book_id         BIGINT              AUTO_INCREMENT PRIMARY KEY,
    book_title      VARCHAR(255)        NOT NULL,
    book_price      DECIMAL(10, 2)      NOT NULL
);

CREATE TABLE tbl_user
(
    user_id         BIGINT                NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_name       VARCHAR(250)          NOT NULL,
    user_password   VARCHAR(250)          NOT NULL,
    user_fk_role    BIGINT                NOT NULL
);

CREATE TABLE tbl_role
(
    role_id         BIGINT                NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role            VARCHAR(250)          NOT NULL
);

-- ------------------------------------------------
-- Constraints FK & Unique
-- ------------------------------------------------
ALTER TABLE tbl_role
    ADD CONSTRAINT UNIQ_ROLE
        UNIQUE (role);

ALTER TABLE tbl_user
    ADD CONSTRAINT FK_TO_ROLE_ID
        FOREIGN KEY (user_fk_role) REFERENCES tbl_role (role_id);

-- ------------------------------------------------
-- Insert users and roles (Updated role names)
-- ------------------------------------------------
insert tbl_role (role) values ('ROLE_ADMIN');
insert tbl_role (role) values ('ROLE_USER');

-- admin's password is: 54321
insert tbl_user (user_name, user_password, user_fk_role)
       values ('admin', '$2a$12$gfTMWrXUwBU.eVPVYbz9C.dPg9kFfRCfL8oYa1TOZg63QCD8nKi1C', 1 );

-- ana's password is: 12345
insert tbl_user (user_name, user_password, user_fk_role)
       values ('ana', '$2a$12$Q6gFWzwrEUUiaF4kD1M3tOqvuV1N1txnf9hxZtkAk8jLb3U5Gjv.O', 2 );

-- ------------------------------------------------
-- Insert sample book data
-- ------------------------------------------------
INSERT INTO tbl_book (book_title, book_price)
VALUES ('The Secrets of the Universe', 19.99),
       ('Adventures in Spring Boot', 25.50),
       ('Mastering Thymeleaf', 29.99),
       ('The Art of MySQL', 35.00),
       ('Bootstrap for Beginners', 15.75),
       ('Deep Dive into JDBC', 27.45),
       ('Spring Security Unleashed', 32.99),
       ('Building Scalable APIs', 40.00),
       ('Java Persistence in Action', 22.50),
       ('Microservices with Spring', 38.95);
```
- Now run the application and see that all endpoints are by default **protected**.

## Creating the custom `login.html`
- We need to create our custom `login.html` HTML form inside `<root-project-folder>/src/main/resources/templates/login.html` folder:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

    <head>
        <meta charset="UTF-8">
        <title>Login</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    </head>

    <body>
        <div class="container m-4 w-50">
            <h2>Login</h2>

            <form th:action="@{/login}" method="post">
                <div class="mb-3">
                    <label class="form-label">Username</label>
                    <input type="text" class="form-control" name="username" required>
                </div>
                <div class="mb-3">
                    <label class="form-label">Password</label>
                    <input type="password" class="form-control" name="password" required>
                </div>

                <button type="submit" class="btn btn-primary">Login</button>
                <a href="/books" class="btn btn-danger">Cancel</a>
            </form>

            <div th:if="${param.error}" class="alert alert-danger mt-3">
                Invalid username or password.
            </div>

            <div th:if="${param.logout}" class="alert alert-success mt-3">
                You have been logged out successfully.
            </div>
        </div>
    </body>

</html>
```

- Modify the `BookController.java` to include the endpoint to show the login page:

```java
// New endpoint to show the login.html
@GetMapping( "/login" )
public String login()
{
    return "login"; // Return the custom login page
}
```
## Spring Security Configurations (to unlock some endpoints)
- Create a package `config` and implement the `SecurityConfig.java` with the following code:
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Enables web security and tells Spring to use this class for security configuration.
// Spring looks for a SecurityFilterChain bean defined in the configuration class
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
            .csrf( Customizer.withDefaults() ) // Enable CSRF protection; Uses Spring Security’s default session-based CSRF handling.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/books", "/login").permitAll() // Public pages
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN") // Admin-only actions
                .anyRequest().authenticated() // Everything else requires authentication (i.e, is protected)
            )
            .formLogin(login -> login
                .loginPage("/login") // Custom login page
                .defaultSuccessUrl("/books", true) // Redirect after login to the list of books
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher( "/logout", "POST") ) // Logout via POST
                .logoutSuccessUrl("/login?logout") // Redirect after logout to the login page
                .invalidateHttpSession(true) // Delete cookies and session data
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
```

- Now create the `PasswordEncoderConfig` class to handle encryption passwords inside the `config` package:
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // handle hashing passwords
    }
}
```

## Model Classes `User` and `Role`
- Inside the `model` package, create the `Role` and `User` classes:

```java
// Role class
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table( name = "tbl_role" )
public class Role {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "role_id", updatable = false, nullable = false )
    private Long id;

    @Column( name = "role", nullable = false, length = 250 )
    private String role;

    public Role() {}
    public Role( String role ) { this.role = role; }

    public Long getId() {return id;}
    public String getRole() {return role;}

    @Override public boolean equals( Object otherRole )
    {
        if ( !( otherRole instanceof Role role1 ) ) return false;
        return Objects.equals( role, role1.role );
    }

    @Override public int hashCode()
    {
        return Objects.hashCode( role );
    }
}
```

```java
// User class
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table( name = "tbl_user" )
public class User {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "user_id", updatable = false, nullable = false )
    private Long id;

    @Column( name = "user_name", nullable = false, length = 250 )
    private String name;

    @Column( name = "user_password", nullable = false, length = 250 )
    private String password;

    @OneToOne(
        fetch = FetchType.LAZY,
        optional = false, // NOT NULL; // Required for lazy loading with proxies!
        cascade = CascadeType.PERSIST
    )
    @JoinColumn( name = "user_fk_role",  referencedColumnName = "role_id", unique = true )
    private Role role;

    public User() {}
    public User( String name, String password )
    {
        this.name = name;
        this.password = password;
    }

    public Role getRole() {return role;}
    public void setRole( Role role ) {this.role = role;}

    public Long getId() {return id;}
    public String getName() {return name;}
    public String getPassword() {return password;}

    @Override public boolean equals( Object o )
    {
        if ( !( o instanceof User user ) ) return false;
        return Objects.equals( name, user.name ) && Objects.equals( password, user.password );
    }

    @Override public int hashCode()
    {
        return Objects.hash( name, password );
    }
}
```

## Authentication using database credentials
- Create the `MyUsernamePwdAuthenticationProvider` class inside the `config` package:

```java
package com.example.books.config;

import com.example.books.model.Role;
import com.example.books.model.User;
import com.example.books.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/* The interface which we need to implement to define the logic on how a user should be
 * authenticated inside Spring Security framework is AuthenticationProvider
 */
@Component
public class MyUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* The authenticate(Authentication authentication) method represents all the logic for authentication. */
    @Override
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException
    {
        String htmlFormUser = authentication.getName();
        String htmlFormPassword = String.valueOf( authentication.getCredentials() );


        /* Busca o usuário no banco de dados com base no nome informado no formulário HTML */
        User fetchedUser = userRepository.findUserWithRoleByName( htmlFormUser )
                                         .orElse( null );

       /*
        * Essa verificação substitui a chamada padrão ao UserDetailsService e PasswordEncoder.
        * Aqui, comparamos o usuário buscado e validamos sua senha de forma segura.
        * This condition generally calls UserDetailsService and PasswordEncoder to test the username and password.
        */
        if ( ( fetchedUser != null ) &&
             ( fetchedUser.getId() > 0 ) &&
             // Em vez de comparar diretamente com fetchedUser.getPassword().equals(htmlFormPassword), usamos o PasswordEncoder
             passwordEncoder.matches( htmlFormPassword, fetchedUser.getPassword() ) ) // usando BcryptEncoder
        {
            // From Spring Security in Action 2nd edition:
            // This class is an implementation of the Authentication interface and represents a
            // standard authentication request with username and password
            return new UsernamePasswordAuthenticationToken(
               /*
                * O primeiro parâmetro (fetchedUser.getName()) será usado pelo Spring Security
                * como o "nome de usuário autenticado". Por exemplo, no endpoint /dashboard,
                * o nome exibido será esse.
                *
                * Se preferíssemos autenticar pelo e-mail, bastaria ajustar a lógica de autenticação
                * e retornar fetchedUser.getEmail() aqui.
                *
                * Como o segundo parâmetro (credentials) não é mais necessário após a autenticação,
                * passamos null.
                */
                fetchedUser.getName(), null, getGrantedAuthorities( fetchedUser.getRole() )
            );
        } else
        {
            throw new AuthenticationCredentialsNotFoundException( "Invalid credentials!" );
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities( Role role )
    {
        /* From Spring Security in Action 2nd edition:
         * "GrantedAuthority: It represents a privilege granted to the user. A user must have at
         * least one authority. To create an authority, you only need to find a name for that
         * privilege. Another possibility is to use the SimpleGrantedAuthority class to create
         * authority instances. The SimpleGrantedAuthority class offers a way to create immutable
         * instances of the type GrantedAuthority. Spring Security uses authorities to refer either
         * to fine-grained privileges or to roles, which are groups of privileges."
         */
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(
            new SimpleGrantedAuthority( role.getRole().toUpperCase() )
        );
        return grantedAuthorities;
    }

   /*  Informa ao Spring Security que o nosso AuthenticationProvider suporta (como DaoAuthenticationProvider)
    *  autenticações do tipo UsernamePasswordAuthenticationToken.
    *  O ProviderManager usa este método para decidir qual AuthenticationProvider usar
    *  com base no tipo do objeto Authentication recebido.
    */
    @Override
    public boolean supports( Class<?> authenticationType )
    {
        // From Spring Security in Action 2nd edition:
        // "type/style of the Authentication implementation here"
        // Then we must decide what kind of Authentication interface
        // implementation this AuthenticationProvider supports.
        // Implement the supports (Class<?> authentication) method to specify which type of
        // authentication is supported by the AuthenticationProvider that we define.
        // That depends on what type we expect to be provided as a parameter to the authenticate()method
        // em outras palavras, o tipo de autenticação que nosso AuthenticationProvider suportará
        return UsernamePasswordAuthenticationToken.class
                    .isAssignableFrom( authenticationType );
    }
}
```

## Spring Repositories for User and Role classes

- Inside the package `repository`, create the Spring repository interfaces:

```java
import com.example.books.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
```

```java
import com.example.books.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query( "SELECT u FROM User u JOIN FETCH u.role WHERE u.name = :name" )
    Optional<User> findUserWithRoleByName( @Param( "name" ) String name );
}
```

## Changing `books.html` page

- Now, modify the `books.html` page to include security configuration:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security" lang="en">

    <head>
        <meta charset="UTF-8">
        <title>Book List</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    </head>

    <body>
        <div class="container mt-4">
            <h2 class="mb-4">Books List</h2>

            <!-- Login button visible to unauthenticated users -->
            <div sec:authorize="!isAuthenticated()">
                <a th:href="@{/login}" class="btn btn-success mb-3">Login</a>
            </div>

            <!-- Visible Only to Authenticated Users -->
            <!-- Roles are displayed without brackets, and bold font -->
            <div sec:authorize="isAuthenticated()">
                <p>Welcome, <span sec:authentication="name"></span>!</p>
                <p>Your roles:
                    <span th:each="role, iterStat : ${#authentication.authorities}">
                        <b th:text="${#strings.substringAfter(role.authority, 'ROLE_')}"></b>
                        <span th:if="${!iterStat.last}">, </span>
                    </span>
                </p>
            </div>

            <!-- Visible Only to Admins Users -->
            <a sec:authorize="hasRole('ROLE_ADMIN')"
               th:href="@{/books/add}" class="btn btn-primary mb-3">Add New Book</a>

            <!-- Visible Only to Authenticated Users -->
            <div sec:authorize="isAuthenticated()" style="display:inline;">
                <form th:action="@{/logout}" method="post" style="display:inline;">
                    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
                    <button type="submit" class="btn btn-success mb-3">Logout</button>
                </form>
            </div>

            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Price $</th>
                        <th sec:authorize="isAuthenticated() and !hasRole('ROLE_USER')">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="book : ${books}">
                        <td th:text="${book.id}"></td>
                        <td th:text="${book.title}"></td>
                        <td th:text="${book.price}"></td>
                        <td sec:authorize="isAuthenticated()">
                            <!-- Edit Button (Only for Admins) -->
                            <a sec:authorize="hasRole('ROLE_ADMIN')" th:href="@{/books/edit/{id}(id=${book.id})}"
                               class="btn btn-warning btn-sm">Edit</a>
                            <!-- Delete Button (Only for Admins) -->
                            <a sec:authorize="hasRole('ROLE_ADMIN')" th:href="@{/books/delete/{id}(id=${book.id})}"
                               class="btn btn-danger btn-sm"
                               onclick="return confirm('Are you sure you want to delete this book?');">Delete</a>
                        </td>
                    </tr>
                </tbody>
            </table>
            <a href="/" class="btn btn-dark">Home</a>
        </div>
    </body>

</html>
```

- That's it! Just compile everything and run the application.