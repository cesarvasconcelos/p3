## How to run the app during development stage:

- `mvn clean package -DskipTests` to **package** and create a Jar file **without** running tests
- `mvn spring-boot:start` **to run** the app using Jar file
- `mvn spring-boot:stop` **to stop** the app using Jar file

## New changes from earlier Spring Books Application:

- In `books.html`, add the Edit & Delete buttons:
```html
<td>
    <!--Edit Button -->
    <a th:href="@{/books/edit/{id}(id=${book.id})}" class="btn btn-warning btn-sm">Edit</a>
    <!-- Delete Button  -->
    <a th:href="@{/books/delete/{id}(id=${book.id})}" class="btn btn-danger btn-sm"
        onclick="return confirm('Are you sure you want to delete this book?');">Delete</a>
</td>
```

- In `BookController`, add the following methods:

```java
    // Handle book removal
    @GetMapping( "/books/delete/{id}" )
    public String deleteBook( @PathVariable Long id )
    {
        bookService.deleteById( id );
        return "redirect:/books";
    }

    // Display edit book form
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
    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("book") Book book,
                             BindingResult result) {
        if (result.hasErrors()) {
            return "edit_book";
        }

        // Ensure we only update title and price, not the ID
        Optional<Book> existingBook = bookService.findById(id);
        if (existingBook.isPresent()) {
            Book updatedBook = existingBook.get();
            updatedBook.setTitle(book.getTitle());
            updatedBook.setPrice(book.getPrice());
            bookService.save(updatedBook);
        }

        return "redirect:/books"; // Redirect after updating
    }

```
- In `BookService`, add the following methods:
```java
    public Optional<Book> findById( Long id ) { return bookRepository.findById( id ); }
```

- Add `edit_book.html` :
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>Edit Book</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
              rel="stylesheet">
    </head>
    <body>

        <div class="container mt-5">
            <h2>Edit Book</h2>

            <form method="post" th:action="@{/books/edit/{id}(id=${book.id})}" th:object="${book}">
                <!-- Global Error Alert -->
                <div th:if="${#fields.hasErrors('*')}" class="alert alert-danger">
                    Please fix the errors below and try again.
                </div>

                <input th:field="*{id}" type="hidden"/>  <!-- Keep ID hidden (not editable) -->

                <!-- Title Input -->
                <div class="mb-3">
                    <label class="form-label">Title</label>
                    <input type="text" class="form-control"
                           th:field="*{title}"
                           th:classappend="${#fields.hasErrors('title')} ? 'is-invalid' : (${book.title != null and !book.title.isBlank()} ? 'is-valid' : '')"
                           required>
                    <!-- Error message for invalid input -->
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('title')}" th:errors="*{title}"></div>
                    <!-- Valid feedback when there are no errors and the field is filled -->
                    <div class="valid-feedback" th:if="${not #fields.hasErrors('title') and book.title != null}">Looks good!</div>
                </div>

                <!-- Price Input -->
                <div class="mb-3">
                    <label class="form-label">Price</label>
                    <input type="number" step="0.01" class="form-control"
                           th:field="*{price}"
                           th:classappend="${#fields.hasErrors('price')} ? 'is-invalid' : (${book.price > 0} ? 'is-valid' : '')">
                    <!-- Error message for invalid input -->
                    <div class="invalid-feedback" th:if="${#fields.hasErrors('price')}" th:errors="*{price}"></div>
                    <!-- Valid feedback for correct input -->
                    <div class="valid-feedback" th:unless="${#fields.hasErrors('price')}" th:if="${book.price > 0}">Looks good!</div>
                </div>

                <!-- Submit Button -->
                <button class="btn btn-success" type="submit">Save Changes</button>
                <a class="btn btn-danger" th:href="@{/books}">Cancel</a>
            </form>
        </div>

    </body>
</html>
```
