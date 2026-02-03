package com.dockerplatform.backend.security;

import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.repositories.UserRepo;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
//        logger.info("[UserDetailsService] Loading user by email: {}", username);

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("[UserDetailsService] No user found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

//        logger.info("[UserDetailsService] Found user: {} with role: {}", user.getUsername(), user.getRole());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
    }
}