package com.example.books.service;

import com.example.books.model.Book;
import com.example.books.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<Book> findById( Long aLong )
    {
        return bookRepository.findById( aLong );
    }

    @Transactional
    public void updateBook(Long id, Book newData)
    {
        // Load managed entity from database
        Optional<Book> existingBook = bookRepository.findById(id);
        existingBook.ifPresent( fetchedBook -> {
            // Ensure we only update title and price, not the book.ID
            fetchedBook.setTitle( newData.getTitle() );
            fetchedBook.setPrice( newData.getPrice() );
            // bookRepository.save( book ); // can be omitted if @Transactional is present
            // No call to save — Hibernate "Dirty Checking" will detect changes and persist on transaction commit
        });
    }
}
