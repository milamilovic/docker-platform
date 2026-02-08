package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping(path = "/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto){

        return userService.register(userDto, UserRole.REGULAR)
                .map(user -> ResponseEntity.ok(user))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<?>> getUser(){
        return ResponseEntity.ok(userService.getAdmins());
    }

    @PostMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody UserDto userDto){
        userDto.setPassword("admin");
        return userService.register(userDto, UserRole.ADMIN)
                .map(user -> ResponseEntity.ok(user))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }


}
