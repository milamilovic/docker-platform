package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.dto.RepositorySearchDTO;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.enums.BadgeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            r.modifiedAt, 
            r.badge
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
            r.modifiedAt, 
            r.badge
        )
        FROM Repository r
        WHERE r.isPublic = true
        ORDER BY r.numberOfStars DESC
    """)
    Page<RepositorySearchDTO> findTopStarred(Pageable pageable);

    @Query(
            value = """
                SELECT *
                FROM repository r
                WHERE r.badge = :badge
                  AND (
                        NULLIF(:search, '') IS NULL
                        OR r.name ILIKE CONCAT('%', :search, '%')
                        OR r.description ILIKE CONCAT('%', :search, '%')
                  )
                ORDER BY r.number_of_pulls DESC
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM repository r
                WHERE r.badge = :badge
                  AND (
                        NULLIF(:search, '') IS NULL
                        OR r.name ILIKE CONCAT('%', :search, '%')
                        OR r.description ILIKE CONCAT('%', :search, '%')
                  )
                """,
            nativeQuery = true
    )
    Page<Repository> findFilteredOfficial(
            @Param("badge") String badge,
            @Param("search") String search,
            Pageable pageable
    );

}
