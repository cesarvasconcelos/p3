# Spring Security com Autenticação - Guia Passo a Passo

## Pré-requisitos
- Java 21
- Maven (o projeto já inclui o Maven Wrapper `mvnw`/`mvnw.cmd`)
- Docker e Docker Compose
- MySQL (via Docker)

## Como executar a aplicação durante o desenvolvimento

- `./mvnw clean package -DskipTests` para **empacotar** e gerar o Jar **sem** rodar os testes
- `./mvnw spring-boot:run` para **rodar** a aplicação diretamente
- Ou use `./mvnw spring-boot:start` para **rodar** a aplicação a partir do Jar e `./mvnw spring-boot:stop` para **pará-la**

> No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

## Configuração do Projeto

### 1. Criar a estrutura do projeto
Crie um novo projeto Spring Boot com a estrutura abaixo ou reutilize a existente neste diretório (`2_spring-security/b_edit-book-with-security/spring-jpa-books`).

### 2. Configurar o `pom.xml`

> **Atenção:** este módulo foi migrado para o `spring-boot-starter-parent` na versão **4.1.1**. Nessa versão alguns starters foram renomeados/reorganizados em relação a versões anteriores do Spring Boot — por exemplo, `spring-boot-starter-web` virou `spring-boot-starter-webmvc`, o Flyway passou a ter um starter próprio (`spring-boot-starter-flyway`) e os starters de teste ganharam variantes dedicadas (`spring-boot-starter-*-test`). Use exatamente as dependências abaixo, que refletem o `pom.xml` atual do projeto:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.1</version>
        <relativePath /> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>spring-jpa-books</artifactId>

    <version>1.0</version>
    <name>spring-jpa-books</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>21</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <maven.compiler.release>${java.version}</maven.compiler.release>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-flyway</artifactId>
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
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.thymeleaf.extras</groupId>
            <artifactId>thymeleaf-extras-springsecurity6</artifactId>
        </dependency>
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-flyway-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-mysql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>bookstore-app</finalName>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <!-- $ mvn spring-boot:start
                    -Dspring-boot.run.main-class=com.example.SpringJdbcBooksApplication -->
                    <mainClass>com.example.books.SpringJdbcBooksApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3. Configuração do Docker Compose

Crie um arquivo `compose.yaml` no diretório raiz do projeto:

```yaml
services:
    mysql:
        image: 'mysql:8.4'
        container_name: db
        volumes:
            #  - ./init.sql:/docker-entrypoint-initdb.d/init.sql # leia more-about-db-initialization.txt
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
        name: "vol_bookstore" # Set volume name
```

> O projeto ainda mantém um `init.sql` na raiz (não utilizado pelo Compose, pois a linha correspondente está comentada). Ele existe apenas como referência histórica de uma inicialização "manual" via `/docker-entrypoint-initdb.d`, hoje substituída pelas migrações do Flyway descritas mais abaixo. Detalhes sobre como o MySQL processa esse tipo de script estão em `more-about-db-initialization.txt`, no mesmo diretório.

Crie um arquivo `.env` no diretório raiz:

```env
MYSQL_ROOT_PASSWORD=secret
MYSQL_USER=cesar
MYSQL_PASSWORD=cesar
MYSQL_DATABASE=db_bookstore
```

### 4. Application Properties

Crie/atualize `src/main/resources/application.properties`:

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

## Configuração do Banco de Dados com Flyway Migrations

Em vez de um único script SQL, este projeto usa migrações do Flyway para um melhor controle de versão do banco de dados.

### Criar os arquivos de migração

Crie os seguintes arquivos em `src/main/resources/db/migration/`:

**V1__create-database.sql:**
```sql
-- ------------------------------------------------
-- Create tables
-- ------------------------------------------------
CREATE TABLE tbl_book
(
    book_id         BIGINT              AUTO_INCREMENT PRIMARY KEY,
    book_title      VARCHAR(255)        NOT NULL,
    -- DECIMAL(10,2): 10 = total de dígitos (precisão), 2 = dígitos após a vírgula (escala)
    -- => 8 dígitos antes da vírgula; valor máximo: 99999999.99
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

-- Each user has a foreign key user_fk_role pointing to tbl_role.role_id.
-- This means that each user must have a role assigned to them.
-- Each role has a unique role name (e.g., ADMIN, USER).
-- One role (e.g., ADMIN) can be referenced by many users.
-- But one user can only have one role in this schema.
-- This is a one-to-many relationship, where one role can be assigned to many users,
-- but each user can only have one role.
```

**V2__insert-users-with-roles.sql:**
```sql
insert tbl_role (role) values ('ROLE_ADMIN');
insert tbl_role (role) values ('ROLE_USER');

insert tbl_user (user_name, user_password, user_fk_role)
       values ('admin', '$2a$12$gfTMWrXUwBU.eVPVYbz9C.dPg9kFfRCfL8oYa1TOZg63QCD8nKi1C', 1 );

insert tbl_user (user_name, user_password, user_fk_role)
       values ('ana', '$2a$12$Q6gFWzwrEUUiaF4kD1M3tOqvuV1N1txnf9hxZtkAk8jLb3U5Gjv.O', 2 );
```

> As senhas em texto puro correspondentes a esses hashes BCrypt estão listadas na seção [Credenciais de Teste](#credenciais-de-teste), mais abaixo.

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

## Classes de Modelo (Model)

### Restrição de Validação Personalizada (Custom Constraint)

Primeiro, crie uma constraint de validação personalizada em `src/main/java/com/example/books/constraints/`:

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

@Documented // Includes the annotation in Javadoc
@Target( { FIELD } ) // Specifies where the annotation can be applied
@Retention( RUNTIME ) // Retains the annotation at runtime
@Constraint( validatedBy = { PriceLimitValidationLogic.class} ) // Links the annotation to its validator class that has business logic
public @interface PriceLimit {
    // Default error message (can be a key to a resource bundle)
    // String message() default "Price must be a value lower than {limit}";

    // Uses annotation attribute inside the message using {limit}
    String message() default "{com.example.books.constraints.priceLimit}";

    // Allows grouping of constraints (e.g., for conditional validation)
    Class<?>[] groups() default {};

    // Allows attaching metadata to the constraint
    Class<? extends Payload>[] payload() default {};

    // Custom attribute for the annotation (e.g., specifies the expected case)
    double limit() default 500D;
}
```

**PriceLimitValidationLogic.java:**

> ⚠️ Corrigido em relação à versão anterior deste guia: a implementação real usa `BigDecimal` (evitando problemas de arredondamento binário do `double`) e compara com `<` (estritamente menor que o limite), não `<=`.

```java
package com.example.books.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class PriceLimitValidationLogic implements ConstraintValidator<PriceLimit, BigDecimal> {
    private BigDecimal upperLimit;

    @Override public void initialize( PriceLimit annotation )
    {
        // this.upperLimit = BigDecimal.valueOf( annotation.limit() );
        this.upperLimit = new BigDecimal( String.valueOf( annotation.limit() ) );
    }

    @Override public boolean isValid( BigDecimal valueOfBookPriceField, ConstraintValidatorContext context )
    {
        if ( valueOfBookPriceField == null )
        {
            return true; // Consider null values as valid
        }

        return valueOfBookPriceField.compareTo( upperLimit ) < 0;
    }
}
```

### Model Book

Crie `src/main/java/com/example/books/model/Book.java`:

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
    @NotBlank( message = "Title is required" ) // not null, not empty string, not spaces/tabs only
    @Column( name = "book_title", nullable = false )
    private String title;

    @NotNull( message = "Price is required" ) // ensures the price is present.
    @Positive( message = "Price must be greater than zero" )
    @PriceLimit(limit = 3000D) // my customized constraint annotation
    @Column( name = "book_price", nullable = false, precision = 10, scale = 2 )
    private BigDecimal price;

    public Book()
    {
    }

    public Book( Long id, String title, double price )
    {
        this.setId( id );
        this.setTitle( title );
        this.setPrice( BigDecimal.valueOf(  price  ) );
    }

    public Long getId() {return id;}
    public void setId( Long id ) {this.id = id;}

    public String getTitle() {return title;}
    public void setTitle( String title ) {this.title = title;}

    public BigDecimal getPrice() {return price;}
    public void setPrice( BigDecimal price ) {this.price = price;}

    @Override public String toString()
    {
        return "Book{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", price=" + price +
               '}';
    }

    @Override public boolean equals( Object other )
    {
        if ( !( other instanceof Book book ) ) return false;
        return Objects.equals( id, book.id );
    }

    @Override public int hashCode()
    {
        return Objects.hashCode( id );
    }
}
```

### Models User e Role

Crie `src/main/java/com/example/books/model/Role.java`:

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

Crie `src/main/java/com/example/books/model/User.java`:

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

> As importações `CascadeType` e `OneToOne` não são usadas por `User` atualmente — foram mantidas porque assim está no código-fonte do módulo. Se for feita uma limpeza desses imports, atualize este guia também.

## Camada de Repositório (Repository)

Crie as interfaces de repositório em `src/main/java/com/example/books/repository/`:

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

## Camada de Serviço (Service)

Crie `src/main/java/com/example/books/service/BookService.java`:

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

    public BookService( BookRepository bookRepository )
    {
        this.bookRepository = bookRepository;
    }

    @Transactional( readOnly = true )
    public List<Book> findAll()
    {
        return bookRepository.findAll();
    }

    @Transactional
    public Book save( Book entity )
    {
        return bookRepository.save( entity );
    }

    @Transactional
    public void deleteById( Long aLong )
    {
        bookRepository.deleteById( aLong );
    }

    @Transactional( readOnly = true )
    public Optional<Book> findById( Long aLong ) { return bookRepository.findById( aLong ); }

    @Transactional
    public Optional<Book> updateBook(Long id, String title, BigDecimal price )
    {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book updatedBook = existingBook.get();
            updatedBook.setTitle(title);
            updatedBook.setPrice(price);
            // No need for explicit repo.save() - dirty checking will handle it
            return Optional.of(updatedBook);
        }
        return Optional.empty();
    }
}
```

> `updateBook` atualiza o título e o preço de um livro aproveitando o **dirty checking** do Hibernate: como a entidade `updatedBook` foi carregada dentro da transação (via `findById`), qualquer alteração feita nos seus setters é detectada e persistida automaticamente no `commit`, sem a necessidade de chamar `bookRepository.save(...)` explicitamente.

## Configuração do Spring Security

Crie as classes de configuração em `src/main/java/com/example/books/config/`:

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
@EnableWebSecurity // Enables web security and tells Spring to use this class for security configuration. Spring looks for a SecurityFilterChain bean defined in the configuration class
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
            .csrf( Customizer.withDefaults() ) // Enable CSRF protection; Uses Spring Security’s default session-based CSRF handling.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/books", "/login").permitAll() // Public pages
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN") // Admin-only actions
                .anyRequest().authenticated() // Everything else requires authentication
            )
            .formLogin(login -> login
                .loginPage("/login") // Custom login page
                .defaultSuccessUrl("/books", true) // Redirect after login (Setting a default success URL for the login form)
                .failureUrl( "/login?error" ) // Redirect after login failed (default behaviour of wrong username/password)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")  // Define the URL that triggers logout
                .logoutSuccessUrl("/login?logout") // Redirect after logout success (default behaviour of Logout success)
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

## Camada de Controller

Crie `src/main/java/com/example/books/controller/BookController.java`:

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

    public BookController( BookService bookService )
    {
        this.bookService = bookService;
    }

    @GetMapping( "/login" )
    public String login()
    {
        return "login"; // Return the custom login page
    }

    @GetMapping( "/books" )
    public String listBooks( Model model )
    {
        model.addAttribute( "books", bookService.findAll() );
        return "books";
    }

    // Roda antes de QUALQUER endpoint desta classe; o retorno entra no Model como "book".
    // Útil quando o mesmo objeto/dado é necessário em vários endpoints (evita repetir
    // model.addAttribute em cada um); aqui, quem se beneficia de fato desta instância
    // ser adicionada ao Model é showAddForm()/addBook().
    @ModelAttribute( "book" )
    private Book bindBookToHtmlForm()
    {
        return new Book(); // Initialize an empty Book "COMMAND OBJECT"
    }

    @GetMapping( "/books/add" )
    public String showAddForm()
    {
        return "add_book";
    }

    // "book" abaixo vem por convenção do tipo Book (a classe e não o nome do parâmetro), mesmo
    // sem @ModelAttribute("book") explícito. Funciona, mas a prática recomendada é
    // escrever o nome mesmo quando redundante, como faz o updateBook mais abaixo:
    // fica claro pra quem lê qual chave a view espera, e evita quebra silenciosa se
    // a classe Book for renomeada no futuro.
    // O BindingResult também precisa vir IMEDIATAMENTE após o parâmetro @Valid, senão
    // o Spring nem chama este método em caso de erro (HandlerMethodValidationException
    // → HTTP 400).
    @PostMapping( "/books/add" )
    public String addBook( @Valid @ModelAttribute Book book, BindingResult result )
    {
        if ( result.hasErrors() )
        {
            // O Spring já colocou "book" (reaproveitando o que bindBookToHtmlForm() criou,
            // agora com os dados digitados) e o BindingResult no Model antes desta linha
            // rodar; por isso o formulário volta preenchido sem precisarmos chamar
            // model.addAttribute aqui.
            return "add_book";
        }
        bookService.save( book );
        return "redirect:/books"; // Redirect to books list
    }

    // Endpoint to delete a book given its <id>
    @GetMapping( "/books/delete/{id}" )
    public String deleteBook( @PathVariable Long id )
    {
        bookService.deleteById( id );
        return "redirect:/books"; // Redirect to books list
    }

    // Display edit book form
    // Aqui SOMOS nós que colocamos "book" no Model (GET), o oposto do POST abaixo,
    // onde é o próprio Spring quem publica o "book" no Model automaticamente, antes
    // do método rodar.
    @GetMapping("/books/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model)
    {
        Optional<Book> book = bookService.findById( id );
        if (book.isPresent()) {
            model.addAttribute("book", book.get());
            return "edit_book";
        }
        return "redirect:/books"; // Redirect if book not found
    }

    // Mesma regra de ordem do addBook: BindingResult logo após o parâmetro @Valid.
    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("book") Book book,
                             BindingResult result) {
        if (result.hasErrors()) {
            // É o próprio Spring quem publica o "book" no Model automaticamente, antes
            // do método rodar; por isso não precisamos chamar model.addAttribute aqui
            // ao retornar "edit_book" em caso de erro.
            return "edit_book";
        }

        // O "book" aqui foi preenchido só com os campos que o formulário enviou,
        // e não com os dados do banco; o id só chegou porque edit_book.html tem um
        // <input type="hidden" th:field="*{id}">. Por isso delegamos ao service, que
        // busca a entidade original pelo id e altera (ou modifica) apenas title/price.
        // Delegate the update logic to the service layer (better separation of concerns)
        bookService.updateBook(id, book.getTitle(), book.getPrice());
        return "redirect:/books"; // Redirect after updating
    }
}
```

## Templates HTML

Crie os seguintes templates em `src/main/resources/templates/`. Todos usam **Bootstrap 5.3.8** (versão fixa/pinada, já em uso no restante do módulo) e o **Bootstrap Icons 1.11.3**.

**index.html:**
```html
<!doctype html>
<html lang="en">

    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Bootstrap demo</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
        <!-- Bootstrap Icons CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>

    <body>
        <div class="container mt-4">
            <h1><i class="bi bi-book me-3"></i>Welcome to Bookstore</h1>
            <hr>
            <a href="/books" class="btn btn-primary"> <i class="bi bi-list-ul me-2"></i> Book List</a>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI"
            crossorigin="anonymous"></script>
    </body>

</html>
```

> O `<title>` desta página ainda está como "Bootstrap demo" (resquício do boilerplate inicial do Bootstrap) em vez de "Bookstore". É uma inconsistência cosmética existente no código-fonte; corrija-a lá se quiser, e então atualize este bloco.

**login.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

    <head>
        <meta charset="UTF-8">
        <title>Login</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet"
              integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
        <!-- Bootstrap Icons CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>

    <body>
        <div class="container m-4 w-50">
            <h2> <i class="bi bi-box-arrow-in-right me-2"></i>
                 Login
            </h2>
            <p class="text-muted">Please enter your username and password to access your account.</p>
            <form th:action="@{/login}" method="post">
                <!-- Thymeleaf automatically adds CSRF token here -->
                <!--<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />-->
                <div class="mb-3">
                    <label class="form-label">
                        <i class="bi bi-person-fill me-1"></i>
                        Username</label>
                    <input type="text" class="form-control" name="username" placeholder="Enter your username" required autofocus>
                </div>
                <div class="mb-3">
                    <label class="form-label">
                        <i class="bi bi-lock-fill me-1"></i>
                        Password</label>
                    <input type="password" class="form-control" name="password" placeholder="Enter your password" required>
                </div>

                <button type="submit" class="btn btn-primary">
                     <i class="bi bi-box-arrow-in-right me-2"></i>Login
                </button>
                <a href="/books" class="btn btn-danger">
                    <i class="bi bi-arrow-left-circle me-2"></i>Cancel</a>
            </form>
            <!-- Error Messages -->
            <div th:if="${param.error}" class="alert alert-danger alert-dismissible fade show mt-3" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <span>Invalid <strong>username</strong> or <strong>password</strong>.</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>

            <!-- Success Messages -->
            <div th:if="${param.logout}" class="alert alert-success alert-dismissible fade show mt-3" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <span>You have been logged out successfully.</span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
                integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI" crossorigin="anonymous"></script>
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
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
        <!-- Bootstrap Icons CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>

    <body>
        <div class="container mt-4">
            <h2 class="mb-4"><i class="bi bi-list-ul me-3"></i>Books List</h2>

            <!-- Login button visible to all users -->
            <div sec:authorize="!isAuthenticated()">
                <a th:href="@{/login}" class="btn btn-primary">
                     <i class="bi bi-box-arrow-in-right me-2"></i>Login</a>
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
               th:href="@{/books/add}" class="btn btn-primary mb-3"><i class="bi bi-plus-circle me-2"></i>Add New Book</a>

            <!-- Visible Only to Authenticated Users -->
            <div sec:authorize="isAuthenticated()" style="display:inline;">
                <form th:action="@{/logout}" method="post" style="display:inline;">
                    <!-- Thymeleaf automatically adds CSRF token here -->
                    <!--<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />-->
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
                            <!-- Edit Button (Only for Admins) -->
                            <a sec:authorize="hasRole('ROLE_ADMIN')" th:href="@{/books/edit/{id}(id=${book.id})}"
                               class="btn btn-warning btn-sm"><i class="bi bi-pencil-square me-2"></i>Edit</a>
                            <!-- Delete Button (Only for Admins) -->
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
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB" crossorigin="anonymous">
        <!-- Bootstrap Icons CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>

    <body>
        <div class="container mt-4">
            <h2><i class="bi bi-plus-circle-fill me-3"></i>Add New Book</h2>
            <p class=" text-muted">Fill in the details to add a new book to the bookstore</p>
            <form th:action="@{/books/add}" method="post" th:object="${book}" class="needs-validation">
                <!-- Thymeleaf automatically adds CSRF token here -->
                <!--<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />-->
                <!-- Global Error Alert -->
                <div th:if="${#fields.hasErrors('*')}" class="alert alert-danger">
                    Please fix the errors below and try again.
                </div>

                <!-- Book Title Field -->
                <div class="mb-3">
                    <label class="form-label">
                         <i class="bi bi-book me-1"></i>Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control"
                           th:field="*{title}"
                           th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : (${book.title != null} ? 'is-valid' : '')"
                           required autofocus>
                    <!-- Error message for invalid input -->
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}"></div>
                    <!-- Valid feedback for correct input -->
                    <div class="valid-feedback" th:unless="${#fields.hasErrors('title')}" th:if="${book.title != null}">Looks good!</div>
                </div>

                <!-- Price -->
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
                        <!-- Error message for invalid input -->
                        <div class="invalid-feedback" th:if="${#fields.hasErrors('price')}" th:errors="*{price}"></div>
                        <!-- Valid feedback for correct input -->
                        <div class="valid-feedback" th:unless="${#fields.hasErrors('price')}" th:if="${book.price > 0}">Looks good!</div>
                    </div>
                    <div class="form-text">Price must be between $0.01 and $3000.00</div>
                </div>

                <!-- Submit Button -->
                <button type="submit" class="btn btn-primary mb-3"><i class="bi bi-plus-circle me-2"></i>Add Book</button>
            </form>
            <hr>
            <a href="/books" class="btn btn-danger"><i class="bi bi-arrow-left-circle me-2"></i>Cancel</a>

            <form th:action="@{/logout}" method="post" style="display:inline;">
                <!-- Thymeleaf automatically adds CSRF token here -->
                <!--<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />-->
                <button type="submit" class="btn btn-success"><i class="bi bi-box-arrow-right me-2"></i>Logout</button>
            </form>

            <!-- Form Help -->
            <div class="row mt-4">
                <div class="col-lg-8">
                    <div class="card bg-light">
                        <div class="card-body">
                            <h6 class="card-title">
                                <i class="bi bi-info-circle-fill text-info me-2"></i>Form Guidelines
                            </h6>
                            <ul class="mb-0 small">
                                <li>All fields marked with <span class="text-danger">*</span> are required</li>
                                <li>Title must not be blank</li>
                                <li>Price must be between $0.01 and $3000.00</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>

</html>
```

**edit_book.html:**

> Este template difere consideravelmente da versão anterior deste guia: o botão de submit passou a se chamar "Save Changes" (com ícone `bi-save`), há um botão "Cancel" que leva de volta para `/books`, o campo `id` fica oculto dentro do próprio `<form>`, e o formulário de Logout foi removido em favor do link Cancel. Um card de "Form Guidelines" também foi adicionado, igual ao de `add_book.html`.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>Edit Book</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css"
              rel="stylesheet">
        <!-- Bootstrap Icons CSS -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
    </head>
    <body>

        <div class="container mt-5">
            <h2 class="mb-3"><i class="bi bi-pencil-fill me-3"></i>Edit Book</h2>
            <p class=" text-muted">Update the book information below</p>
            <form method="post" th:action="@{/books/edit/{id}(id=${book.id})}" th:object="${book}">
                <!-- Thymeleaf automatically adds CSRF token here -->
                <!--<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />-->
                <!-- Global Error Alert -->
                <div th:if="${#fields.hasErrors('*')}" class="alert alert-danger">
                    Please fix the errors below and try again.
                </div>

                <input th:field="*{id}" type="hidden"/>  <!-- Keep ID hidden (not editable) -->

                <!-- Book Title Field -->
                <div class="mb-3">
                    <label class="form-label">
                         <i class="bi bi-book me-1"></i>Title <span class="text-danger">*</span></label>
                    <input type="text" class="form-control"
                           th:field="*{title}"
                           th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : (${book.title != null and !book.title.isBlank()} ? 'is-valid' : '')"
                           required autofocus>
                    <!-- Error message for invalid input -->
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}"></div>
                    <!-- Valid feedback when there are no errors and the field is filled -->
                    <div class="valid-feedback" th:if="${not #fields.hasErrors('title') and book.title != null}">Looks good!</div>
                </div>

                <div class="mb-3">
                    <label class="form-label"><i class="bi bi-currency-dollar me-1"></i>Price <span class="text-danger">*</span></label>
                    <div class="input-group">
                        <span class="input-group-text">$</span>
                        <input type="number" class="form-control"
                               th:field="*{price}"
                               th:classappend="${#fields.hasErrors('price')} ? 'is-invalid' : (${book.price > 0} ? 'is-valid' : '')"
                               step="0.01" placeholder="0.00" required>
                        <!-- Error message for invalid input -->
                        <div class="invalid-feedback" th:if="${#fields.hasErrors('price')}" th:errors="*{price}"></div>
                        <!-- Valid feedback for correct input -->
                        <div class="valid-feedback" th:unless="${#fields.hasErrors('price')}" th:if="${book.price > 0}">Looks good!</div>
                    </div>
                    <div class="form-text">Price must be between $0.01 and $3000.00</div>
                </div>

                <!-- Submit Button -->
                <button class="btn btn-success" type="submit"><i class="bi bi-save me-2"></i> Save Changes</button>
                <a class="btn btn-danger" th:href="@{/books}"><i class="bi bi-arrow-left-circle me-2"></i>Cancel</a>
            </form>

            <!-- Form Help -->
            <div class="row mt-4">
                <div class="col-lg-8">
                    <div class="card bg-light">
                        <div class="card-body">
                            <h6 class="card-title">
                                <i class="bi bi-info-circle-fill text-info me-2"></i>Form Guidelines
                            </h6>
                            <ul class="mb-0 small">
                                <li>All fields marked with <span class="text-danger">*</span> are required</li>
                                <li>Title must not be blank</li>
                                <li>Price must be between $0.01 and $3000.00</li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </body>
</html>
```

## Mensagens de Validação

Crie `src/main/resources/ValidationMessages.properties`:

```properties
com.example.books.constraints.priceLimit=Price must not exceed a limit of ${limit}
```

## Executando a Aplicação

1. **Subir o Docker Compose (banco MySQL):**
   ```bash
   docker compose -f compose.yaml up -d
   ```

2. **Rodar a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Acessar a aplicação:**
   - Página inicial: http://localhost:8080/
   - Lista de livros: http://localhost:8080/books
   - Página de login: http://localhost:8080/login

4. **Encerrar o ambiente:**
   ```bash
   docker compose -f compose.yaml down -v
   ```

## Credenciais de Teste

- **Usuário admin:**
  - Usuário: `admin`
  - Senha: `54321`
  - Pode adicionar, editar e excluir livros

- **Usuário comum:**
  - Usuário: `ana`
  - Senha: `12345`
  - Pode apenas visualizar os livros

## Principais Funcionalidades

- **Segurança:** controle de acesso baseado em papéis (roles) com Spring Security
- **Banco de dados:** MySQL com migrações via Flyway
- **Validação:** anotações de validação padrão do Bean Validation e uma constraint personalizada (`@PriceLimit`)
- **Interface:** UI responsiva estilizada com Bootstrap 5.3.8 e Bootstrap Icons
- **Docker:** banco de dados containerizado via Docker Compose
- **Testes:** suporte a Testcontainers para testes de integração

Pronto! A aplicação está pronta para ser compilada e executada com autenticação e autorização completas.
