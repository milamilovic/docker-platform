package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Star;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StarRepo extends JpaRepository<Star, UUID> {
}
