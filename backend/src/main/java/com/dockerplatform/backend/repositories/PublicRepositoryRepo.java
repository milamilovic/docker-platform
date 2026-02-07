package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.dto.RepositorySearchDTO;
import com.dockerplatform.backend.models.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PublicRepositoryRepo extends JpaRepository<Repository, UUID>, JpaSpecificationExecutor<Repository> {

    @Query("""
        SELECT new com.dockerplatform.backend.dto.RepositorySearchDTO(
            r.id,
            r.name,
            r.description,
            r.ownerUsername,
            r.numberOfPulls,
            r.numberOfStars,
            r.isOfficial,
            r.createdAt,
            r.modifiedAt
        )
        FROM Repository r
        WHERE r.isPublic = true
        ORDER BY r.numberOfPulls DESC
    """)
    Page<RepositorySearchDTO> findTopPulled(Pageable pageable);

    @Query("""
        SELECT new com.dockerplatform.backend.dto.RepositorySearchDTO(
            r.id,
            r.name,
            r.description,
            r.ownerUsername,
            r.numberOfPulls,
            r.numberOfStars,
            r.isOfficial,
            r.createdAt, 
            r.modifiedAt
        )
        FROM Repository r
        WHERE r.isPublic = true
        ORDER BY r.numberOfStars DESC
    """)
    Page<RepositorySearchDTO> findTopStarred(Pageable pageable);

}
