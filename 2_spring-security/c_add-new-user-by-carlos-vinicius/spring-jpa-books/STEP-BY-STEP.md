# Spring Security with Authentication - Step by Step Guide

## Prerequisites
- Java 21
- Maven
- Docker and Docker Compose
- MySQL (via Docker)

## How to run the app during development stage:

- `mvn clean package -DskipTests` to **package** and create a Jar file **without** running tests
- `mvn spring-boot:run` **to run** the app directly
- Or use `mvn spring-boot:start` **to run** the app using Jar file and `mvn spring-boot:stop` **to stop** it

## Project Setup

### 1. Create the project structure
Create a new Spring Boot project with the following structure or use the existing one.

### 2. Configure `pom.xml`
Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-mysql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>

    <!-- Runtime Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-docker-compose</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Test Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.platform</groupId>
        <artifactId>junit-platform-suite-engine</artifactId>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 3. Docker Compose Configuration

Create a `compose.yaml` file in the root directory:

```yaml
services:
    mysql:
        image: 'mysql:8.1'
        container_name: db
        volumes:
            - vol_bookstore:/var/lib/mysql
        environment:
            - 'MYSQL_DATABASE=${MYSQL_DATABASE}'
            - 'MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}'
            - 'MYSQL_USER=${MYSQL_USER}'
            - 'MYSQL_PASSWORD=${MYSQL_PASSWORD}'
        ports:
            - '3306:3306'
volumes:
    vol_bookstore:
        name: "vol_bookstore"
```

Create a `.env` file in the root directory:

```env
MYSQL_ROOT_PASSWORD=secret
MYSQL_USER=cesar
MYSQL_PASSWORD=cesar
MYSQL_DATABASE=db_bookstore
```

### 4. Application Properties

Create/update `src/main/resources/application.properties`:

```properties
# Thymeleaf cache (disable in dev)
spring.thymeleaf.cache=false

# Docker container will not be closed when application shutdown
#spring.docker.compose.lifecycle-management=start_only

# Whether Docker Compose support is enabled
#spring.docker.compose.enabled=false

# Optional: Enable SQL logging to see Hibernate dirty checking in action
# https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html
# spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type=TRACE
logging.level.org.hibernate.type.descriptor.sql=TRACE
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

logging.file.name=logs/bookstore.log
logging.file.path=logs
```

## Database Setup with Flyway Migrations

Instead of using a single SQL script, this project uses Flyway migrations for better database version control.

### Create Migration Files

Create the following files in `src/main/resources/db/migration/`:

**V1__create-database.sql:**
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
```

**V2__insert-users-with-roles.sql:**
```sql
insert tbl_role (role) values ('ROLE_ADMIN');
insert tbl_role (role) values ('ROLE_USER');

-- admin's password is: 54321
insert tbl_user (user_name, user_password, user_fk_role)
       values ('admin', '$2a$12$gfTMWrXUwBU.eVPVYbz9C.dPg9kFfRCfL8oYa1TOZg63QCD8nKi1C', 1 );

-- ana's password is: 12345
insert tbl_user (user_name, user_password, user_fk_role)
       values ('ana', '$2a$12$Q6gFWzwrEUUiaF4kD1M3tOqvuV1N1txnf9hxZtkAk8jLb3U5Gjv.O', 2 );
```

**V3__insert-books.sql:**
```sql
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

## Model Classes

### Custom Validation Constraint

First, create a custom validation constraint in `src/main/java/com/example/books/constraints/`:

**PriceLimit.java:**
```java
package com.example.books.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target( { FIELD } )
@Retention( RUNTIME )
@Constraint( validatedBy = { PriceLimitValidationLogic.class} )
public @interface PriceLimit {
    String message() default "{com.example.books.constraints.priceLimit}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    double limit() default 500D;
}
```

**PriceLimitValidationLogic.java:**
```java
package com.example.books.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class PriceLimitValidationLogic implements ConstraintValidator<PriceLimit, BigDecimal> {
    private double limit;

    @Override
    public void initialize(PriceLimit constraintAnnotation) {
        this.limit = constraintAnnotation.limit();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return value == null || value.doubleValue() <= limit;
    }
}
```

### Book Model

Create `src/main/java/com/example/books/model/Book.java`:

```java
package com.example.books.model;

import com.example.books.constraints.PriceLimit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table( name = "tbl_book" )
public class Book {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "book_id", nullable = false, updatable = false )
    private Long id;

    @Size( max = 255 )
    @NotBlank( message = "Title is required" )
    @Column( name = "book_title", nullable = false )
    private String title;

    @NotNull( message = "Price is required" )
    @Positive( message = "Price must be greater than zero" )
    @PriceLimit(limit = 3000D)
    @Column( name = "book_price", nullable = false, precision = 10, scale = 2 )
    private BigDecimal price;

    public Book() {}

    public Book( Long id, String title, double price ) {
        this.setId( id );
        this.setTitle( title );
        this.setPrice( BigDecimal.valueOf( price ) );
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId( Long id ) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle( String title ) { this.title = title; }

    public BigDecimal getPrice() { return price; }
    public void setPrice( BigDecimal price ) { this.price = price; }

    @Override
    public boolean equals( Object other ) {
        if ( !( other instanceof Book book ) ) return false;
        return Objects.equals( id, book.id );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode( id );
    }
}
```

### User and Role Models

Create `src/main/java/com/example/books/model/Role.java`:

```java
package com.example.books.model;

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

    public Long getId() { return id; }
    public String getRole() { return role; }

    @Override
    public boolean equals( Object otherRole ) {
        if ( !( otherRole instanceof Role role1 ) ) return false;
        return Objects.equals( role, role1.role );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode( role );
    }
}
```

Create `src/main/java/com/example/books/model/User.java`:

```java
package com.example.books.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_fk_role", nullable = false, referencedColumnName = "role_id")
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

## Repository Layer

Create the repository interfaces in `src/main/java/com/example/books/repository/`:

**BookRepository.java:**
```java
package com.example.books.repository;

import com.example.books.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
```

**RoleRepository.java:**
```java
package com.example.books.repository;

import com.example.books.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
```

**UserRepository.java:**
```java
package com.example.books.repository;

import com.example.books.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findUserWithRoleByName(String name);
}
```

## Service Layer

Create `src/main/java/com/example/books/service/BookService.java`:

```java
package com.example.books.service;

import com.example.books.model.Book;
import com.example.books.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService( BookRepository bookRepository ) {
        this.bookRepository = bookRepository;
    }

    @Transactional( readOnly = true )
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Transactional
    public Book save( Book entity ) {
        return bookRepository.save( entity );
    }

    @Transactional
    public void deleteById( Long aLong ) {
        bookRepository.deleteById( aLong );
    }

    @Transactional( readOnly = true )
    public Optional<Book> findById( Long aLong ) {
        return bookRepository.findById( aLong );
    }

    /**
     * Updates a book's title and price using Hibernate dirty checking.
     * This method encapsulates the business logic for updating books,
     * ensuring proper separation of concerns.
     *
     * @param id The book ID to update
     * @param title The new title
     * @param price The new price
     * @return Optional containing the updated book, or empty if not found
     */
    @Transactional
    public Optional<Book> updateBook(Long id, String title, BigDecimal price ) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book updatedBook = existingBook.get();
            updatedBook.setTitle(title);
            updatedBook.setPrice(price);
            // No need for explicit repo.save() - Hibernate dirty checking handles it automatically
            return Optional.of(updatedBook);
        }
        return Optional.empty();
    }
}
```

## Spring Security Configuration

Create the configuration classes in `src/main/java/com/example/books/config/`:

**SecurityConfig.java:**
```java
package com.example.books.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
            .csrf( Customizer.withDefaults() )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/books", "/login").permitAll()
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/books", true)
                .failureUrl( "/login?error" )
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
```

**PasswordEncoderConfig.java:**
```java
package com.example.books.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**MyUsernamePwdAuthenticationProvider.java:**
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

@Component
public class MyUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException {
        String htmlFormUser = authentication.getName();
        String htmlFormPassword = String.valueOf( authentication.getCredentials() );

        User fetchedUser = userRepository.findUserWithRoleByName( htmlFormUser )
                                         .orElse( null );

        if ( ( fetchedUser != null ) &&
             ( fetchedUser.getId() > 0 ) &&
             passwordEncoder.matches( htmlFormPassword, fetchedUser.getPassword() ) ) {

            return new UsernamePasswordAuthenticationToken(
                fetchedUser.getName(), null, getGrantedAuthorities( fetchedUser.getRole() )
            );
        } else {
            throw new AuthenticationCredentialsNotFoundException( "Invalid credentials!" );
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities( Role role ) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(
            new SimpleGrantedAuthority( role.getRole().toUpperCase() )
        );
        return grantedAuthorities;
    }

    @Override
    public boolean supports( Class<?> authenticationType ) {
        return UsernamePasswordAuthenticationToken.class
                    .isAssignableFrom( authenticationType );
    }
}
```

## Controller Layer

Create `src/main/java/com/example/books/controller/BookController.java`:

```java
package com.example.books.controller;

import com.example.books.model.Book;
import com.example.books.service.BookService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController( BookService bookService ) {
        this.bookService = bookService;
    }

    @GetMapping( "/login" )
    public String login() {
        return "login";
    }

    @GetMapping( "/books" )
    public String listBooks( Model model ) {
        model.addAttribute( "books", bookService.findAll() );
        return "books";
    }

    @ModelAttribute( "book" )
    private Book bindBookToHtmlForm() {
        return new Book();
    }

    @GetMapping( "/books/add" )
    public String showAddForm() {
        return "add_book";
    }

    @PostMapping( "/books/add" )
    public String addBook( @Valid @ModelAttribute Book book, BindingResult result ) {
        if ( result.hasErrors() ) {
            return "add_book";
        }
        bookService.save( book );
        return "redirect:/books";
    }

    @GetMapping( "/books/delete/{id}" )
    public String deleteBook( @PathVariable Long id ) {
        bookService.deleteById( id );
        return "redirect:/books";
    }

    @GetMapping("/books/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Optional<Book> book = bookService.findById( id );
        if (book.isPresent()) {
            model.addAttribute("book", book.get());
            return "edit_book";
        }
        return "redirect:/books";
    }

    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("book") Book book,
                             BindingResult result) {
        if (result.hasErrors()) {
            return "edit_book";
        }

        // Delegate the update logic to the service layer (better separation of concerns)
        bookService.updateBook(id, book.getTitle(), book.getPrice());
        return "redirect:/books"; // Redirect after updating
    }
}
```

## HTML Templates

Create the following templates in `src/main/resources/templates/`:

**index.html:**
```html
<!doctype html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Bookstore</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>
    <body>
        <div class="container mt-4">
            <h1><i class="bi bi-book me-3"></i>Welcome to Bookstore</h1>
            <hr>
            <a href="/books" class="btn btn-primary"> <i class="bi bi-list-ul me-2"></i> Book List</a>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>
    </body>
</html>
```

**login.html:**
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

**books.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security" lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Book List</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>
    <body>
        <div class="container mt-4">
            <h2 class="mb-4"><i class="bi bi-list-ul me-3"></i>Books List</h2>

            <!-- Login button visible to unauthenticated users -->
            <div sec:authorize="!isAuthenticated()">
                <a th:href="@{/login}" class="btn btn-primary">
                     <i class="bi bi-box-arrow-in-right me-2"></i>Login</a>
            </div>

            <!-- Visible Only to Authenticated Users -->
            <div sec:authorize="isAuthenticated()">
                <p>Welcome, <span sec:authentication="name"></span>!</p>
                <p>Your roles:
                    <span th:each="role, iterStat : ${#authentication.authorities}">
                        <b th:text="${#strings.substringAfter(role.authority, 'ROLE_')}"></b>
                        <span th:if="${!iterStat.last}">, </span>
                    </span>
                </p>
            </div>

            <!-- Visible Only to Admin Users -->
            <a sec:authorize="hasRole('ROLE_ADMIN')"
               th:href="@{/books/add}" class="btn btn-primary mb-3"><i class="bi bi-plus-circle me-2"></i>Add New Book</a>

            <!-- Logout button for authenticated users -->
            <div sec:authorize="isAuthenticated()" style="display:inline;">
                <form th:action="@{/logout}" method="post" style="display:inline;">
                    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
                    <button type="submit" class="btn btn-success mb-3"><i class="bi bi-box-arrow-right me-2"></i>Logout</button>
                </form>
            </div>

            <table class="table table-striped">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Price $</th>
                        <th sec:authorize="hasRole('ROLE_ADMIN')">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="book : ${books}">
                        <td th:text="${book.id}"></td>
                        <td th:text="${book.title}"></td>
                        <td th:text="${book.price}"></td>
                        <td sec:authorize="hasRole('ROLE_ADMIN')">
                            <a sec:authorize="hasRole('ROLE_ADMIN')" th:href="@{/books/edit/{id}(id=${book.id})}"
                               class="btn btn-warning btn-sm"><i class="bi bi-pencil-square me-2"></i>Edit</a>
                            <a sec:authorize="hasRole('ROLE_ADMIN')" th:href="@{/books/delete/{id}(id=${book.id})}"
                               class="btn btn-danger btn-sm"
                               onclick="return confirm('Are you sure you want to delete this book?');"><i class="bi bi-trash me-2"></i>Delete</a>
                        </td>
                    </tr>
                </tbody>
            </table>
            <a href="/" class="btn btn-dark"><i class="bi bi-house-door me-2"></i>Home</a>
        </div>
    </body>
</html>
```

**add_book.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>Add Book</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>
    <body>
        <div class="container mt-4">
            <h2><i class="bi bi-plus-circle-fill me-3"></i>Add New Book</h2>
            <p class="text-muted">Fill in the details to add a new book to the bookstore</p>

            <form th:action="@{/books/add}" method="post" th:object="${book}" class="needs-validation">
                <div th:if="${#fields.hasErrors('*')}" class="alert alert-danger">
                    Please fix the errors below and try again.
                </div>

                <div class="mb-3">
                    <label class="form-label">
                         <i class="bi bi-book me-1"></i>Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control"
                           th:field="*{title}"
                           th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : (${book.title != null} ? 'is-valid' : '')"
                           required autofocus>
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}"></div>
                    <div class="valid-feedback" th:unless="${#fields.hasErrors('title')}" th:if="${book.title != null}">Looks good!</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">
                        <i class="bi bi-currency-dollar me-1"></i>Price <span class="text-danger">*</span>
                    </label>
                    <div class="input-group">
                        <span class="input-group-text">$</span>
                        <input type="number" class="form-control"
                               th:field="*{price}"
                               th:classappend="${#fields.hasErrors('price')} ? 'is-invalid' : (${book.price > 0} ? 'is-valid' : '')"
                               step="0.01" placeholder="0.00" required>
                        <div class="invalid-feedback" th:if="${#fields.hasErrors('price')}" th:errors="*{price}"></div>
                        <div class="valid-feedback" th:unless="${#fields.hasErrors('price')}" th:if="${book.price > 0}">Looks good!</div>
                    </div>
                    <div class="form-text">Price must be between $0.01 and $3000.00</div>
                </div>

                <button type="submit" class="btn btn-primary mb-3"><i class="bi bi-plus-circle me-2"></i>Add Book</button>
            </form>

            <hr>
            <a href="/books" class="btn btn-danger"><i class="bi bi-arrow-left-circle me-2"></i>Cancel</a>

            <form th:action="@{/logout}" method="post" style="display:inline;">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
                <button type="submit" class="btn btn-success"><i class="bi bi-box-arrow-right me-2"></i>Logout</button>
            </form>
        </div>
    </body>
</html>
```

**edit_book.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>Edit Book</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>
    <body>
        <div class="container mt-4">
            <h2><i class="bi bi-pencil-square me-3"></i>Edit Book</h2>

            <form th:action="@{/books/edit/{id}(id=${book.id})}" method="post" th:object="${book}">
                <div th:if="${#fields.hasErrors('*')}" class="alert alert-danger">
                    Please fix the errors below and try again.
                </div>

                <div class="mb-3">
                    <label class="form-label">
                         <i class="bi bi-book me-1"></i>Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control"
                           th:field="*{title}"
                           th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : 'is-valid'"
                           required autofocus>
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}"></div>
                </div>

                <div class="mb-3">
                    <label class="form-label">
                        <i class="bi bi-currency-dollar me-1"></i>Price <span class="text-danger">*</span>
                    </label>
                    <div class="input-group">
                        <span class="input-group-text">$</span>
                        <input type="number" class="form-control"
                               th:field="*{price}"
                               th:classappend="${#fields.hasErrors('price')} ? 'is-invalid' : 'is-valid'"
                               step="0.01" required>
                        <div class="invalid-feedback" th:if="${#fields.hasErrors('price')}" th:errors="*{price}"></div>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary mb-3"><i class="bi bi-check-circle me-2"></i>Update Book</button>
            </form>

            <hr>
            <a href="/books" class="btn btn-danger"><i class="bi bi-arrow-left-circle me-2"></i>Cancel</a>

            <form th:action="@{/logout}" method="post" style="display:inline;">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
                <button type="submit" class="btn btn-success"><i class="bi bi-box-arrow-right me-2"></i>Logout</button>
            </form>
        </div>
    </body>
</html>
```

## Validation Messages

Create `src/main/resources/ValidationMessages.properties`:

```properties
com.example.books.constraints.priceLimit=Price must not exceed ${limit}
```

## Running the Application

1. **Start Docker Compose:**
   ```bash
   docker-compose up -d
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application:**
   - Home page: http://localhost:8080/
   - Books list: http://localhost:8080/books
   - Login page: http://localhost:8080/login

## Test Credentials

- **Admin user:**
  - Username: `admin`
  - Password: `54321`
  - Can add, edit, and delete books

- **Regular user:**
  - Username: `ana`
  - Password: `12345`
  - Can only view books

## Key Features

- **Security:** Role-based access control with Spring Security
- **Database:** MySQL with Flyway migrations
- **Validation:** Custom and built-in validation annotations
- **UI:** Bootstrap-styled responsive interface with icons
- **Docker:** Containerized database setup
- **Testing:** Testcontainers support for integration tests

That's it! The application is now ready to compile and run with full authentication and authorization features.