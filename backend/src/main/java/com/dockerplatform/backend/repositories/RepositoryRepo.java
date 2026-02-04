package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepo extends JpaRepository<Repository, UUID> {
    List<Repository> findByOwner(User owner);

    List<Repository> findByIsOfficial(boolean isOfficial);

    List<Repository> findByIsPublic(boolean isPublic);

    Optional<Repository> findByOwnerAndName(User owner, String name);

    boolean existsByOwnerAndName(User owner, String name);

    boolean existsByName(String name);
}
