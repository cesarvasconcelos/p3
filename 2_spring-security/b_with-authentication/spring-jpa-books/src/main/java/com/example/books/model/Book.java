package com.example.books.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table( name = "tbl_book" )
public class Book {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "book_id", nullable = false, updatable = false )
    private Long id;

    @Size( max = 255 )
    @NotBlank( message = "Title is required" ) // not null, not empty string, not spaces/tabs only
    @Column( name = "book_title", nullable = false )
    private String title;

    @NotNull( message = "Price is required" ) // ensures the price is present.
    @Positive( message = "Price must be greater than zero" )
    @Column( name = "book_price", nullable = false, precision = 10, scale = 2 )
    private BigDecimal price;

    public Book()
    {
    }

    public Book( Long id, String title, double price )
    {
        this.setId( id );
        this.setTitle( title );
        this.setPrice( BigDecimal.valueOf(  price  ) );
    }

    public Long getId() {return id;}
    public void setId( Long id ) {this.id = id;}

    public String getTitle() {return title;}
    public void setTitle( String title ) {this.title = title;}

    public BigDecimal getPrice() {return price;}
    public void setPrice( BigDecimal price ) {this.price = price;}

    @Override public String toString()
    {
        return "Book{" +
               "id=" + id +
               ", title='" + title + '\'' +
               ", price=" + price +
               '}';
    }

    @Override public boolean equals( Object other )
    {
        if ( !( other instanceof Book book ) ) return false;
        return Objects.equals( id, book.id );
    }

    @Override public int hashCode()
    {
        return Objects.hashCode( id );
    }
}