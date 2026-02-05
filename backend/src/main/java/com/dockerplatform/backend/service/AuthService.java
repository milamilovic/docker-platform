package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.AuthRequest;
import com.dockerplatform.backend.dto.AuthResponse;
import com.dockerplatform.backend.exceptions.PasswordChangeRequiredException;
import com.dockerplatform.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

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

        if("superadmin".equals(dto.username())){
            Path secretFile = Path.of("secrets","super_admin.txt");
            if (Files.exists(secretFile)){
                throw new PasswordChangeRequiredException();
            }
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        return new AuthResponse(jwtService.generateToken(userService.findByUsername(dto.username())));
    }
}
