package com.example.books.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table( name = "tbl_user" )
public class User {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "user_id", updatable = false, nullable = false )
    private Long id;

    @Column( name = "user_name", nullable = false, length = 250 )
    private String name;

    @Column( name = "user_password", nullable = false, length = 250 )
    private String password;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_fk_role", nullable = false, referencedColumnName = "role_id")
    private Role role;

    public User() {}
    public User( String name, String password )
    {
        this.name = name;
        this.password = password;
    }

    public Role getRole() {return role;}
    public void setRole( Role role ) {this.role = role;}

    public Long getId() {return id;}
    public String getName() {return name;}
    public String getPassword() {return password;}

    @Override public boolean equals( Object o )
    {
        if ( !( o instanceof User user ) ) return false;
        return Objects.equals( name, user.name ) && Objects.equals( password, user.password );
    }

    @Override public int hashCode()
    {
        return Objects.hash( name, password );
    }
}