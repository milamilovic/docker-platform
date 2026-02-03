package com.dockerplatform.backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/hello")
public class HelloController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String hello() {
        return "Hello from backend!";
    }

}
