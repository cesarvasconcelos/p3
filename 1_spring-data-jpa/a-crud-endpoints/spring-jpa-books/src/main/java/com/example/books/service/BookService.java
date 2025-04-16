package com.example.books.service;

import com.example.books.model.Book;
import com.example.books.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService( BookRepository bookRepository )
    {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll()
    {
        return bookRepository.findAll();
    }

    @Transactional
    public Book save( Book entity )
    {
        return bookRepository.save( entity );
    }

    public void deleteById( Long aLong )
    {
        bookRepository.deleteById( aLong );
    }
}
