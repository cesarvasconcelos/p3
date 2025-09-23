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

    // Este método vai ser invocado antes ao invocar qualquer endpoint
    // Para fins didáticos: neste exemplo específico, talvez nem precisasse de @ModelAttribute
    // pois apenas os endpoints showAddForm() e addBook() se beneficiam desta instância do Book
    // adicionada ao modelo. Os endpoints listBooks() e deleteBook() não utilizam este objeto.
    // Porém, se houvesse múltiplos endpoints que necessitassem de um objeto Book no modelo,
    // faria mais sentido usar @ModelAttribute para evitar repetição de código.
    //
    // QUANDO então um método marcado com @ModelAttribute seria útil?
    // - Quando múltiplos endpoints precisam do mesmo objeto no modelo (ex: formulários de edição e visualização)
    // - Para inicializar objetos com valores padrão passados para todas as views
    // - Para carregar dados compartilhados (ex: listas de categorias de livros, ...) que
    //   são usados em vários endpoints diferentes
    // - Para evitar duplicação de código quando o mesmo objeto é usado em diferentes views
    @ModelAttribute( "book" )
    private Book prepareBookForModel()
    {
        return new Book(); // Initialize an empty Book "COMMAND OBJECT" to be used in forms
    }

    @GetMapping( "/books/add" )
    public String showAddForm()
    {
        return "add_book";
    }

    @PostMapping( "/books/add" )
    public String addBook( @Valid @ModelAttribute( "book" ) Book book, BindingResult result )
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