package com.example.user.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.user.repository.UserRepository;
import com.example.user.model.UserEntity;
import com.example.user.model.UserLoggedIn;

/**
 * 
 * <b>CustomUserDetailsService</b><br>
 * 
 * Service class that implements UserDetailsService interface.
 * It is used to load user by username during authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepo.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "User not found: " + email
                )
            );

        return new UserLoggedIn(
            user.getIdUser(),
            user.getEmail(),
            user.getPassword(),
            Collections.emptyList()
        );
    }
}
