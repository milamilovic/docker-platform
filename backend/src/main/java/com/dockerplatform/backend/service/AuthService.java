package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.AuthRequest;
import com.dockerplatform.backend.dto.AuthResponse;
import com.dockerplatform.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserService userService;

    public AuthResponse authenticateUser(AuthRequest dto) throws AuthenticationException {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        String token = jwtService.generateToken(userService.findByUsername(dto.username()));
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        return authResponse;
    }
}
