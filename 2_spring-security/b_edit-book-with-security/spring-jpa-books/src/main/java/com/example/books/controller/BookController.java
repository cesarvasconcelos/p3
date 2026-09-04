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