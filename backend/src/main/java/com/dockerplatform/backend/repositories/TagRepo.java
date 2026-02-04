package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepo extends JpaRepository<Tag, UUID> {
    List<Tag> findByRepository(Repository repository);

    Optional<Tag> findByRepositoryAndName(Repository repository, String name);

    boolean existsByRepositoryAndName(Repository repository, String name);

    void deleteByRepository(Repository repository);
}
