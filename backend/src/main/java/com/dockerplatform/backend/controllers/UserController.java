package com.dockerplatform.backend.controllers;

import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
