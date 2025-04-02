package com.example.books;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BookController {
    private final BookRepository repository;

    public BookController( BookRepository repository ) {this.repository = repository;}

    @GetMapping
    public String showAddBookForm( Model model )
    {
        model.addAttribute("book", new Book());
        return "add-book";
    }

    @PostMapping( "/add-book" )
    public String saveBook( Book book )
    {
        repository.save( book );
        return "redirect:/?success";
    }
}
