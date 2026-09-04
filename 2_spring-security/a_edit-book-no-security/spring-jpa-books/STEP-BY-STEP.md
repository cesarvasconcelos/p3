## Como executar a aplicação durante o desenvolvimento

- `mvn clean package -DskipTests` para **empacotar** e gerar o arquivo Jar **sem** rodar os testes
- `mvn spring-boot:start` **para rodar** a aplicação a partir do Jar
- `mvn spring-boot:stop` **para parar** a aplicação que foi iniciada a partir do Jar

## Versão do projeto (`pom.xml`)

Este módulo foi migrado para o `spring-boot-starter-parent` **4.1.1** (Java 21), diferente da
linha de base **3.5.x** usada nos módulos anteriores do curso. Essa migração trouxe uma mudança
de nomenclatura nas dependências relevantes para este passo a passo:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
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
    <!-- ... demais dependências (Flyway, MySQL driver, Docker Compose, testcontainers) ... -->
</dependencies>
```

> **Atenção:** no Spring Boot 3.5.x o starter web se chamava `spring-boot-starter-web`. A partir
> do Spring Boot 4.x ele passou a se chamar **`spring-boot-starter-webmvc`**. Além disso, os
> starters de teste (`*-test`, como `spring-boot-starter-data-jpa-test`,
> `spring-boot-starter-webmvc-test`, `spring-boot-starter-validation-test`,
> `spring-boot-starter-thymeleaf-test` e `spring-boot-starter-flyway-test`) são específicos do
> Boot 4.x — antes bastava o `spring-boot-starter-test` genérico. Consulte o `pom.xml` do módulo
> para a lista completa e atualizada.

## O que mudou em relação à aplicação anterior (funcionalidades de Editar e Apagar)

- Em `books.html`, adicione os botões de Editar e Apagar na coluna de ações da tabela:
```html
<td>
    <!--Edit Button -->
    <a th:href="@{/books/edit/{id}(id=${book.id})}" class="btn btn-warning btn-sm">Edit</a>
    <!-- Delete Button  -->
    <a th:href="@{/books/delete/{id}(id=${book.id})}" class="btn btn-danger btn-sm"
        onclick="return confirm('Are you sure you want to delete this book?');">Delete</a>
</td>
```

- Em `BookController`, adicione os métodos abaixo:

```java
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

    // Handle book edit form submission
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
        bookService.updateBook(id, book.getTitle(), book.getPrice());
        return "redirect:/books"; // Redirect after updating
    }
```

- Em `BookService`, adicione os métodos abaixo:

```java
    @Transactional( readOnly = true )
    public Optional<Book> findById( Long id ) { return bookRepository.findById( id ); }

    // NEW: Dedicated update method that encapsulates business logic
    @Transactional
    public Optional<Book> updateBook(Long id, String title, BigDecimal price )
    {
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
```

- Adicione o template `edit_book.html`:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

    <head>
        <meta charset="UTF-8">
        <title>Edit Book</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>

    <body>

        <div class="container mt-5">
            <h2>Edit Book</h2>

            <form method="post" th:action="@{/books/edit/{id}(id=${book.id})}" th:object="${book}">
                <!-- Global Error Alert -->
                <div th:if="${#fields.hasErrors('*')}" class="alert alert-danger">
                    Please fix the errors below and try again.
                </div>

                <input th:field="*{id}" type="hidden" /> <!-- Keep ID hidden (not editable) -->

                <!-- Title Input -->
                <div class="mb-3">
                    <label class="form-label">Title</label>
                    <input type="text" class="form-control" th:field="*{title}"
                        th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : (${book.title != null and !book.title.isBlank()} ? 'is-valid' : '')"
                        required>
                    <!-- Error message for invalid input -->
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}"></div>
                    <!-- Valid feedback when there are no errors and the field is filled -->
                    <div class="valid-feedback" th:if="${not #fields.hasErrors('title') and book.title != null}">Looks
                        good!</div>
                </div>

                <!-- Price Input -->
                <div class="mb-3">
                    <label class="form-label">Price</label>
                    <input type="number" step="0.01" class="form-control" th:field="*{price}"
                        th:classappend="${#fields.hasErrors('price')} ? 'is-invalid' : (${book.price > 0} ? 'is-valid' : '')">
                    <!-- Error message for invalid input -->
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('price')}" th:errors="*{price}"></div>
                    <!-- Valid feedback for correct input -->
                    <div class="valid-feedback" th:unless="${#fields.hasErrors('price')}" th:if="${book.price > 0}">
                        Looks good!</div>
                </div>

                <!-- Submit Button -->
                <button class="btn btn-success" type="submit">Save Changes</button>
                <a class="btn btn-danger" th:href="@{/books}">Cancel</a>
            </form>
        </div>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI"
            crossorigin="anonymous"></script>
    </body>

</html>
```

> **Nota sobre a versão do Bootstrap:** o restante do projeto (`books.html`, `add_book.html`)
> usa Bootstrap **5.3.8** fixado via CDN, incluindo o script `bootstrap.bundle.min.js` antes do
> fechamento de `</body>`. Mantenha a mesma versão pinada em `edit_book.html` para não misturar
> versões do Bootstrap 5 na mesma aplicação.

## Melhorias de arquitetura realizadas

### 1. Separação de responsabilidades
- **Antes**: o Controller cuidava tanto das preocupações HTTP quanto da lógica de negócio (buscar o livro existente, atualizar os campos, salvar).
- **Depois**: o Controller cuida apenas das preocupações HTTP; a camada de Service cuida da lógica de negócio.

### 2. Aprimoramento da camada de Service
- Foi adicionado o método dedicado `updateBook()`, que encapsula as regras de negócio da atualização.
- O método garante que apenas `title` e `price` sejam atualizados (regra de negócio).
- Usa `@Transactional` para o gerenciamento correto da transação.

### 3. Otimização via Dirty Checking do Hibernate
- Foi removida a chamada explícita a `bookRepository.save()`.
- A aplicação passa a se apoiar no dirty checking automático do Hibernate dentro dos limites da transação.
- Mais eficiente e alinhado às boas práticas de JPA.

### 4. Benefícios deste refactoring
- **Reuso**: o método `updateBook()` pode ser usado por outras partes da aplicação.
- **Testabilidade**: a lógica de negócio agora é mais fácil de testar isoladamente.
- **Manutenibilidade**: mudanças nas regras de atualização exigem alterações apenas na camada de Service.
- **Performance**: o dirty checking do Hibernate é mais eficiente do que chamadas explícitas de `save()`.
- **Código limpo**: o Controller agora fica focado exclusivamente nas preocupações web.

## Opcional: habilitar log de SQL para observar o Dirty Checking em ação

> Estas propriedades já estão configuradas em `application.properties` neste módulo — a seção
> abaixo documenta o que cada uma faz, caso você precise reproduzir esse comportamento em outro
> projeto.

```properties
# Exibe as instruções SQL formatadas
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Exibe o binding dos parâmetros
logging.level.org.hibernate.orm.jdbc.bind=TRACE
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Exibe detalhes da transação (opcional)
logging.level.org.springframework.transaction=DEBUG
```
