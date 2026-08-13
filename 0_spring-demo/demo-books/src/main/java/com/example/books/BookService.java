package com.example.books;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// BookService contains the business logic for managing books.
// The controller talks to the service, and the service talks to the repository.
// This keeps persistence concerns (BookRepository) out of the controller.

@Service
public class BookService {
    private final BookRepository repository;

    public BookService( BookRepository repository ) {this.repository = repository;}

    @Transactional
    public Book save( Book book )
    {
        return repository.save( book );
    }
}
