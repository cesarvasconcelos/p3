package com.example.books.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table( name = "tbl_role" )
public class Role {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "role_id", updatable = false, nullable = false )
    private Long id;

    @Column( name = "role", nullable = false, length = 250 )
    private String role;

    public Role() {}
    public Role( String role ) { this.role = role; }

    public Long getId() {return id;}
    public String getRole() {return role;}

    @Override public boolean equals( Object otherRole )
    {
        if ( !( otherRole instanceof Role role1 ) ) return false;
        return Objects.equals( role, role1.role );
    }

    @Override public int hashCode()
    {
        return Objects.hashCode( role );
    }
}