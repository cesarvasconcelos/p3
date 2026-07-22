package com.example.books;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

// 1. We need a Book class to receive the book data from the form
// and save it to the database.
// The Book object has the following attributes:
// id, title, price
// Note: For persistence, this class also needs a way to bind its fields
// to database columns so that the data can be stored and retrieved.

@Entity
@Table( name = "book" )
public class Book {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Long id;

    @Column( name = "title", nullable = false )
    private String title;

    @Column( name = "price", nullable = false, precision = 10, scale = 2 )
    private BigDecimal price;

    public Long getId() {return id;}
    public void setId( Long id ) {this.id = id;}

    public String getTitle() {return title;}
    public void setTitle( String title ) {this.title = title;}

    public BigDecimal getPrice() {return price;}
    public void setPrice( BigDecimal price ) {this.price = price;}

}