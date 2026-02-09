package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findByRole(UserRole role);
}
