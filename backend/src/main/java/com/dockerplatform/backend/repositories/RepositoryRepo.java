package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RepositoryRepo extends JpaRepository<Repository, UUID> {
}
