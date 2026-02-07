package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService  {

    @Autowired
    UserRepo userRepo;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    
    public User findByUsername(String username){
        return userRepo.findByUsername(username).orElse(null);
    }
    
    public Optional<User> register(UserDto dto, UserRole role){

        if (userRepo.findByUsername(dto.getUsername()).isPresent()){
            return Optional.empty();
        }

        User user = new User();
        user.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(role);
        User save = userRepo.save(user);

        return  Optional.of(save);
    }
    
}
