package com.example.books.config;

import com.example.books.model.Role;
import com.example.books.model.User;
import com.example.books.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/* The interface which we need to implement to define the logic on how a user should be
authenticated inside Spring Security framework is AuthenticationProvider */
@Component
public class MyUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // The authenticate(Authentication authentication) method represents all the logic for authentication.
    @Override
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException
    {
        String htmlFormUser = authentication.getName();
        String htmlFormPassword = String.valueOf( authentication.getCredentials() );


        // Busca o usuário no banco de dados com base no nome informado no formulário HTML
        User fetchedUser = userRepository.findUserWithRoleByName( htmlFormUser )
                                         .orElse( null );

        /*
        * Essa verificação substitui a chamada padrão ao UserDetailsService e PasswordEncoder.
        * Aqui, comparamos o usuário buscado e validamos sua senha de forma segura.
        * This condition generally calls UserDetailsService and PasswordEncoder to test the username and password.
        */
        if ( ( fetchedUser != null ) &&
             ( fetchedUser.getId() > 0 ) &&
             // Em vez de comparar diretamente com fetchedUser.getPassword().equals(htmlFormPassword), usamos o PasswordEncoder
             passwordEncoder.matches( htmlFormPassword, fetchedUser.getPassword() ) ) // usando BcryptEncoder
        {
            return new UsernamePasswordAuthenticationToken(
                /*
                * O primeiro parâmetro (fetchedUser.getName()) será usado pelo Spring Security
                * como o "nome de usuário autenticado". Por exemplo, no endpoint /dashboard,
                * o nome exibido será esse.
                *
                * Se preferíssemos autenticar pelo e-mail, bastaria ajustar a lógica de autenticação
                * e retornar fetchedUser.getEmail() aqui.
                *
                * Como o segundo parâmetro (credentials) não é mais necessário após a autenticação,
                * passamos null.
                */
                fetchedUser.getName(), null, getGrantedAuthorities( fetchedUser.getRole() )
            );
        } else
        {
            throw new AuthenticationCredentialsNotFoundException( "Invalid credentials!" );
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities( Role role )
    {
        // From Spring Security in Action 2nd edition:
        // "GrantedAuthority: It represents a privilege granted to the user. A user must have at
        // least one authority. To create an authority, you only need to find a name for that
        // privilege. Another possibility is to use the SimpleGrantedAuthority class to create
        // authority instances. The SimpleGrantedAuthority class offers a way to create immutable
        // instances of the type GrantedAuthority. Spring Security uses authorities to refer either
        // to fine-grained privileges or to roles, which are groups of privileges."
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(
            new SimpleGrantedAuthority( "ROLE_" + role.getRole().toUpperCase() )
        );
        return grantedAuthorities;
    }

    @Override
    public boolean supports( Class<?> authenticationType )
    {
        // From Spring Security in Action 2nd edition:
        // "type of the Authentication implementation here"
        return UsernamePasswordAuthenticationToken.class
                    .isAssignableFrom( authenticationType );
    }
}
