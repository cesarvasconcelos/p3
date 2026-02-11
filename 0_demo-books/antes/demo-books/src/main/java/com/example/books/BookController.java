package com.example.books;

@Controller
public class BookController {

    // BookController needs to handle the following requests:
    // 1. Show the HTML form to add a book when a GET request is made to http://localhost:8080/
    // 2. Save the book to the database when a POST request is made to http://localhost:8080/add-book
    //    2.1 Redirect to the add-book form indicating success

    // BookRepository is used by BookController to save the book to the database,
    // so it must be injected into the controller

    // GET http://localhost:8080/
    @GetMapping
    public String showAddBookHtmlForm()
    {
        // 1. Show the HTML form to add a book (add-book.html)
    }

    // POST http://localhost:8080/add-book
    @PostMapping( "/add-book" )
    public String saveBook( Book book )
    {
        // 1. We need a Book class to create an object from the data submitted via the form.
        // 1. Save the book using the repository
        // 2. Redirect to the add-book.html and indicate success
    }
}
