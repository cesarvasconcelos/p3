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

@Controller
public class BookController {

    private final BookService bookService;

    public BookController( BookService bookService ) {this.bookService = bookService;}

    @GetMapping( "/books" )
    public String listBooks( Model model )
    {
        model.addAttribute( "books", bookService.findAll() );
        return "books";
    }

    @ModelAttribute( "book" )
    private Book bindBookObjectToHtmlForm()
    {
        return new Book(); // Initialize an empty Book "COMMAND OBJECT"
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
        return "redirect:/books";
    }
}