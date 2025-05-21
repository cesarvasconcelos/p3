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
    - Quick Tip: you can use the https://dbfiddle.uk/ to easily run and see a preview of the changes

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
-- Insert users and roles
-- ------------------------------------------------
insert tbl_role (role) values ('admin');
insert tbl_role (role) values ('student');

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
        return new BCryptPasswordEncoder(); // handle encrypted passwords
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
- Create the `MyUserNamePwdAuthenticationProvider` class inside the `config` package:

```java
import com.example.books.model.Role;
import com.example.books.model.User;
import com.example.books.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
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
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException
    {
        String htmlFormUser = authentication.getName();
        String htmlFormPassword = authentication.getCredentials()
                                                .toString();

        User fetchedUser = userRepository.findUserWithRoleByName( htmlFormUser )
                                         .orElse( null );

        if ( ( fetchedUser != null ) &&
             ( fetchedUser.getId() > 0 ) &&
             // em vez de: password.equals( fetchedUser.getPassword() ) ), usa-se senha encriptada
             passwordEncoder.matches( htmlFormPassword, fetchedUser.getPassword() ) ) // agora com BcryptEncoder
        {
            return new UsernamePasswordAuthenticationToken(
                // o que for passado como primeiro parâmetro (no caso getName())
                // é o que será usado pelo spring security para fins de mostrar no HTML
                // quem está autenticado. Nesse exemplo, o controller/endpoint
                // irá exibir o nome. mas se eu quisesse, poderia ter autenticado pelo e-mail
                // e passar o e-mail para ser exibido na página html como o login autenticado
                fetchedUser.getName(), null, getGrantedAuthorities( fetchedUser.getRole() )
            );
        } else
        {
            throw new BadCredentialsException( "Invalid credentials!" );
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities( Role role )
    {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add( new SimpleGrantedAuthority( "ROLE_" + role.getRole()
                                                                          .toUpperCase() ) );
        return grantedAuthorities;
    }

    @Override
    public boolean supports( Class<?> authentication )
    {
        return authentication.equals( UsernamePasswordAuthenticationToken.class );
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

            <!-- roles displayed without brackets, and bold font -->
            <div sec:authorize="isAuthenticated()">
                <p>Welcome, <span sec:authentication="name"></span>!</p>
                <p>Your roles:
                    <span th:each="role, iterStat : ${#authentication.authorities}">
                        <b th:text="${#strings.substringAfter(role.authority, 'ROLE_')}"></b>
                        <span th:if="${!iterStat.last}">, </span>
                    </span>
                </p>
            </div>

            <a href="/books/add" class="btn btn-primary mb-3">Add New Book</a>

            <!-- Visible Only to Authenticated Users -->
            <div sec:authorize="isAuthenticated()"  style="display:inline;">
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
                        <th sec:authorize="isAuthenticated()">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="book : ${books}">
                        <td th:text="${book.id}"></td>
                        <td th:text="${book.title}"></td>
                        <td th:text="${book.price}"></td>
                        <td>
                            <!-- Edit Button (Only for Admins) -->
                            <a sec:authorize="hasRole('ADMIN')"
                               th:href="@{/books/edit/{id}(id=${book.id})}"
                               class="btn btn-warning btn-sm">Edit</a>
                            <!-- Delete Button (Only for Admins) -->
                            <a sec:authorize="hasRole('ADMIN')"
                               th:href="@{/books/delete/{id}(id=${book.id})}"
                               class="btn btn-danger btn-sm"
                               onclick="return confirm('Are you sure you want to delete this book?');">Delete</a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </body>
</html>
```

- That's it! Just compile everything and run the application.