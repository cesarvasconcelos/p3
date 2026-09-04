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

    @GetMapping( "/" )
    public String showIndexHtmlPage()
    {
        return "index";
    }

    @GetMapping( "/books" )
    public String listBooks( Model model )
    {
        model.addAttribute( "books", bookService.findAll() );
        return "books";
    }

    // Este método é invocado antes de QUALQUER endpoint desta classe, e o retorno
    // entra no Model sob a chave "book". Para fins didáticos: neste exemplo específico,
    // talvez nem precisasse de @ModelAttribute, pois apenas showAddForm() e addBook()
    // se beneficiam desta instância ser adicionada ao Model antes deles rodarem.
    // listBooks() e deleteBook() não usam este "book", mas o método roda mesmo
    // assim em toda requisição atendida por este controller.
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

    // "book" viria por convenção do tipo Book (a classe e não o nome do parâmetro),
    // mesmo sem @ModelAttribute("book") explícito. Aqui escrevemos o nome mesmo sendo
    // redundante: fica claro pra quem lê qual chave a view espera, e evita quebra
    // silenciosa se a classe Book for renomeada no futuro.
    // O BindingResult também precisa vir IMEDIATAMENTE após o parâmetro @Valid, senão
    // o Spring nem chama este método em caso de erro (HandlerMethodValidationException
    // → HTTP 400).
    @PostMapping( "/books/add" )
    public String addBook( @Valid @ModelAttribute( "book" ) Book book, BindingResult result )
    {
        if ( result.hasErrors() )
        {
            // Dica: o parâmetro @ModelAttribute("book") faz o Spring MVC reaproveitar o
            // objeto Book que prepareBookForModel() já colocou no Model (não cria um novo)
            // e preencher seus campos com os dados do formulário, publicando o resultado
            // no Model com o nome "book", junto do BindingResult correspondente.
            // Por isso, ao retornar "add_book" em caso de erro, o formulário já recebe
            // os valores que foram digitados e as mensagens de validação sem precisar chamar
            // model.addAttribute("book", book) manualmente neste método.
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
