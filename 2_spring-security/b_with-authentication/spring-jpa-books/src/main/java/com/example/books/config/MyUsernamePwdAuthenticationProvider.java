package com.example.books.config;

import com.example.books.model.Role;
import com.example.books.model.User;
import com.example.books.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class MyUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate( Authentication authentication ) throws AuthenticationException
    {
        String htmlFormUser = authentication.getName();
        String htmlFormPassword = authentication.getCredentials()
                                                .toString();

        User fetchedUser = userRepository.findUserWithRoleByName( htmlFormUser )
                                         .orElse( null );

        if ( ( fetchedUser != null ) &&
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
                fetchedUser.getName(), null, getGrantedAuthorities( fetchedUser.getRole() )
            );
        } else
        {
            throw new BadCredentialsException( "Invalid credentials!" );
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities( Role role )
    {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add( new SimpleGrantedAuthority( "ROLE_" + role.getRole()
                                                                          .toUpperCase() ) );
        return grantedAuthorities;
    }

    @Override
    public boolean supports( Class<?> authentication )
    {
        return authentication.equals( UsernamePasswordAuthenticationToken.class );
    }
}
