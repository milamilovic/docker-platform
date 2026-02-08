package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.AuthRequest;
import com.dockerplatform.backend.dto.AuthResponse;
import com.dockerplatform.backend.dto.ChangePasswordRequest;
import com.dockerplatform.backend.exceptions.PasswordChangeRequiredException;
import com.dockerplatform.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService service;

    @PostMapping
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest dto){
        try {
            return ResponseEntity.ok(service.authenticateUser(dto));
        } catch (PasswordChangeRequiredException e){
            return ResponseEntity.status(HttpStatus.UPGRADE_REQUIRED).build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> getSystemStatus() {
        return ResponseEntity.ok(service.isSystemLocked());
    }

    @PostMapping("/initialize")
    public ResponseEntity<Boolean> initializeSystem(@RequestBody ChangePasswordRequest request) {
        if (service.initializeSystem(request)){
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

}
