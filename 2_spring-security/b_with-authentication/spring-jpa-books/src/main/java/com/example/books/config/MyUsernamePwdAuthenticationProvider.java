package com.example.books.config;

import com.example.books.model.Role;
import com.example.books.model.User;
import com.example.books.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
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


        // buscar user pelo htmlFormUser no database
        User fetchedUser = userRepository.findUserWithRoleByName( htmlFormUser )
                                         .orElse( null );

        /* This condition generally calls UserDetailsService and PasswordEncoder to test the username and password. */
        if ( ( fetchedUser != null ) &&
             // ( fetchedUser.getName().equalsIgnoreCase( htmlFormUser ) ) && não precisa pois já buscou no database o htmlFormUser
             ( fetchedUser.getId() > 0 ) &&
             // em vez de: password.equals( person.getPassword() ) ), usa-se senha encriptada
             passwordEncoder.matches( htmlFormPassword, fetchedUser.getPassword() ) ) // agora com BcryptEncoder
        {
            return new UsernamePasswordAuthenticationToken(
                // o que for passado como primeiro parametro (no caso getName())
                // é o que será usado pelo spring security para fins de mostrar
                // quem está autenticado. nesse exemplo, o controller /dashboard endpoint
                // irá exibir o nome. mas se eu quisesse poderia ter autenticado pelo e-mail
                // e passar o e-mail para ser exibido como o login autenticado
                //fetchedUser.getName(), fetchedUser.getPassword(), getGrantedAuthorities( fetchedUser.getRole() )
                fetchedUser.getName(), null, getGrantedAuthorities( fetchedUser.getRole() )
            );
        } else
        {
            throw new AuthenticationCredentialsNotFoundException( "Invalid credentials!" );
            // throw new BadCredentialsException( "Invalid credentials!" );
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
        // type of the Authentication implementation here
        return UsernamePasswordAuthenticationToken.class
                    .isAssignableFrom( authenticationType );
        // return authenticationType.equals( UsernamePasswordAuthenticationToken.class );
    }
}
