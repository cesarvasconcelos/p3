package com.example.books.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Enables web security and tells Spring to use this class for security configuration. Spring looks for a SecurityFilterChain bean defined in the configuration class
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
            .csrf( Customizer.withDefaults() ) // Enable CSRF protection; Uses Spring Security’s default session-based CSRF handling.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/books", "/login").permitAll() // Public pages
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN") // Admin-only actions
                .anyRequest().authenticated() // Everything else requires authentication
            )
            .formLogin(login -> login
                .loginPage("/login") // Custom login page
                .defaultSuccessUrl("/books", true) // Redirect after login (Setting a default success URL for the login form))
                .failureUrl( "/login?error" ) // Redirect after login failed (default behaviour of wrong username/password)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher( "/logout", "POST") ) // Logout via POST
                .logoutSuccessUrl("/login?logout") // Redirect after logout success (default behaviour of Logout success)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}