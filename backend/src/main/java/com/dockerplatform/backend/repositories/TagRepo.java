package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TagRepo extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByRepositoryAndName(Repository repository, String name);
    boolean existsByRepositoryAndName(Repository repository, String name);
    void deleteByRepository(Repository repository);

    @Query(value = "SELECT * FROM tag t WHERE t.repository_id = :repositoryId " +
            "AND (CAST(:search AS TEXT) IS NULL OR " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
            "LOWER(t.digest) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))",
            countQuery = "SELECT COUNT(*) FROM tag t WHERE t.repository_id = :repositoryId " +
                    "AND (CAST(:search AS TEXT) IS NULL OR " +
                    "LOWER(t.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
                    "LOWER(t.digest) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))",
            nativeQuery = true)
    Page<Tag> findByRepositoryWithFilters(
            @Param("repositoryId") UUID repositoryId,
            @Param("search") String search,
            Pageable pageable);
}