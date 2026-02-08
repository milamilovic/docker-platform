package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.ChangePasswordRequest;
import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService  {

    @Autowired
    UserRepo userRepo;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    
    public User findByUsername(String username){
        return userRepo.findByUsername(username).orElse(null);
    }

    public User findById(String id) { return userRepo.findById(UUID.fromString(id)).orElse(null); }

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

    public boolean changePassword(ChangePasswordRequest request) {
        Optional<User> userOptional = userRepo.findByUsername(request.username());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (bCryptPasswordEncoder.matches(request.password(), user.getPassword())) {
                user.setPassword(bCryptPasswordEncoder.encode(request.newPassword()));
                userRepo.save(user);
                return true;
            }
        }
        return false;
    }

    public List<User> getAdmins(){
        return userRepo.findByRole(UserRole.ADMIN);
    }

}
