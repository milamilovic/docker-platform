package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepo extends JpaRepository<Tag, UUID> {
}
