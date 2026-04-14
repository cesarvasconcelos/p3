package com.example.books.service;

import com.example.books.model.User;
import com.example.books.repository.RoleRepository;
import com.example.books.repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional( readOnly = true )
    @EntityGraph( attributePaths = "role" )
    public Optional<User> findUserWithRoleByName( String name )
    {
        return userRepository.findUserWithRoleByName( name );
    }
}
