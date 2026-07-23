package com.example.books;

import org.springframework.boot.SpringApplication;

public class TestSpringJdbcBooksApplication {

    public static void main( String[] args )
    {
        SpringApplication.from( SpringJdbcBooksApplication::main )
                         .with( TestcontainersConfiguration.class )
                         .run( args );
    }

}
