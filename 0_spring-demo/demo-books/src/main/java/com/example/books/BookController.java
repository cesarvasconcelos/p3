package com.example.books;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


// BookController needs to handle the following requests:
// 1. Show the HTML form to add a book when a GET request is made to http://localhost:8080/
// 2. Save the book to the database when a POST request is made to http://localhost:8080/add-book
//    2.1 Redirect to the add-book form indicating success

// BookService is used by BookController to save the book to the database,
// so it must be injected into the controller

@Controller
public class BookController {
    private final BookService service;

    public BookController( BookService service ) {this.service = service;}

    // GET http://localhost:8080/
    // 1. Show the HTML form to add a book (add-book.html)
    @GetMapping
    public String showAddBookForm( Model model )
    {
        model.addAttribute("book", new Book());
        return "add-book";
    }

    // POST http://localhost:8080/add-book
    // 1. We need a Book class to create an object from the data submitted via the form.
    // 2. Save the book using the service
    // 3. Redirect to the add-book.html and indicate success
    @PostMapping( "/add-book" )
    public String saveBook( Book book )
    {
        service.save( book );
        return "redirect:/?success";
    }
}
