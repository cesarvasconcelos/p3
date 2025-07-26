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

    @ModelAttribute( "book" )
    private Book bindBookToHtmlForm()
    {
        return new Book(); // Initialize an empty User COMMAND OBJECT
    }

    @GetMapping( "/books/add" )
    public String showAddForm()
    {
        return "add_book";
    }

    @PostMapping( "/books/add" )
    public String addBook( @Valid @ModelAttribute Book book, BindingResult result )
    {
        if ( result.hasErrors() )
        {
            return "add_book";
        }
        bookService.save( book );
        return "redirect:/books";
    }

    @GetMapping( "/books/delete/{id}" )
    public String deleteBook( @PathVariable Long id )
    {
        bookService.deleteById( id );
        return "redirect:/books"; // Redirect to book list
    }

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

    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("book") Book book,
                             BindingResult result) {
        if (result.hasErrors()) {
            return "edit_book";
        }

        bookService.updateBook(id, book);  // Delegates to dirty-checking logic

        return "redirect:/books"; // Redirect after updating
    }
}