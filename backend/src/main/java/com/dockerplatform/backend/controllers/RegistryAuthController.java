package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.service.RegistryTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class RegistryAuthController {

    private final RegistryTokenService tokenService;

    public RegistryAuthController(RegistryTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/token")
    public Map<String, Object> token(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String scope,
            Authentication authentication
    ) {
        String username = authentication.getName(); // iz Basic auth
        String jwt = tokenService.issue(username, service, scope);

        return Map.of(
                "token", jwt,
                "expires_in", 900
        );
    }
}
