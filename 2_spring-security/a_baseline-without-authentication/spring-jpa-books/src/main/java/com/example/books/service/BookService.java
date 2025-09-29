package com.example.books.service;

import com.example.books.model.Book;
import com.example.books.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService( BookRepository bookRepository )
    {
        this.bookRepository = bookRepository;
    }

    @Transactional( readOnly = true )
    public List<Book> findAll()
    {
        return bookRepository.findAll();
    }

    @Transactional
    public Book save( Book entity )
    {
        return bookRepository.save( entity );
    }

    @Transactional
    public void deleteById( Long aLong )
    {
        bookRepository.deleteById( aLong );
    }

    @Transactional( readOnly = true )
    public Optional<Book> findById( Long aLong ) { return bookRepository.findById( aLong ); }

    @Transactional
    public Optional<Book> updateBook(Long id, String title, BigDecimal price )
    {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book updatedBook = existingBook.get();
            updatedBook.setTitle(title);
            updatedBook.setPrice(price);
            return Optional.of(bookRepository.save(updatedBook));
        }
        return Optional.empty();
    }
}
