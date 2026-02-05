package com.dockerplatform.backend.configs;

import com.dockerplatform.backend.dto.UserDto;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class SystemSetupRunner implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        if (userService.findByUsername("superadmin") == null){

            Path secretFolder = Path.of("secrets");
            Path secretFile = secretFolder.resolve("super_admin.txt");

            if (!Files.exists(secretFolder)) {
                Files.createDirectories(secretFolder);
            }
            String rawPassword = UUID.randomUUID().toString();

            UserDto dto = new UserDto();
            dto.setUsername("superadmin");
            dto.setPassword(rawPassword);
            dto.setEmail("admin@admin.com");

            userService.register(dto, UserRole.SUPER_ADMIN);
            Files.writeString(secretFile,rawPassword);
            System.out.println("Super admin created!");
        }
    }
}
