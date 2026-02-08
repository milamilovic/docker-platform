package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.service.RegistryTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class RegistryAuthController {

    @Autowired
    private RegistryTokenService tokenService;

    @GetMapping("/token")
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String scope,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();
            String jwt = tokenService.issue(username, service, scope);

            return ResponseEntity.ok(Map.of("token", jwt, "expires_in", 900));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "errors", List.of(
                            Map.of(
                                    "code", "DENIED",
                                    "message", "access denied",
                                    "detail", e.getMessage()
                            )
                    )
            ));
        }
    }

}
