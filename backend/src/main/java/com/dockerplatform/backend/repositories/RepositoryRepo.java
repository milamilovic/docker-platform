package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepo extends JpaRepository<Repository, UUID> {

    Optional<Repository> findByOwnerAndName(User owner, String name);
    boolean existsByOwnerAndName(User owner, String name);
    boolean existsByName(String name);

    @Query(value = "SELECT * FROM repository r WHERE r.owner_id = :ownerId " +
            "AND (CAST(:search AS TEXT) IS NULL OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))) " +
            "AND (:visibility = 'all' OR " +
            "(:visibility = 'public' AND r.is_public = true) OR " +
            "(:visibility = 'private' AND r.is_public = false))",
            countQuery = "SELECT COUNT(*) FROM repository r WHERE r.owner_id = :ownerId " +
                    "AND (CAST(:search AS TEXT) IS NULL OR " +
                    "LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
                    "LOWER(r.description) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))) " +
                    "AND (:visibility = 'all' OR " +
                    "(:visibility = 'public' AND r.is_public = true) OR " +
                    "(:visibility = 'private' AND r.is_public = false))",
            nativeQuery = true)
    Page<Repository> findByOwnerWithFilters(
            @Param("ownerId") UUID ownerId,
            @Param("search") String search,
            @Param("visibility") String visibility,
            Pageable pageable);

    @Query(value = "SELECT * FROM repository r WHERE r.is_official = true " +
            "AND (CAST(:search AS TEXT) IS NULL OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))",
            countQuery = "SELECT COUNT(*) FROM repository r WHERE r.is_official = true " +
                    "AND (CAST(:search AS TEXT) IS NULL OR " +
                    "LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
                    "LOWER(r.description) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))",
            nativeQuery = true)
    Page<Repository> findByIsOfficialWithFilters(
            @Param("search") String search,
            Pageable pageable);

    @Query(value = "SELECT * FROM repository r WHERE r.owner_id = :ownerId AND r.is_official = true " +
            "AND (CAST(:search AS TEXT) IS NULL OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))",
            countQuery = "SELECT COUNT(*) FROM repository r WHERE r.owner_id = :ownerId AND r.is_official = true " +
                    "AND (CAST(:search AS TEXT) IS NULL OR " +
                    "LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
                    "LOWER(r.description) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')))",
            nativeQuery = true)
    Page<Repository> findByOwnerAndIsOfficialWithFilters(
            @Param("ownerId") UUID ownerId,
            @Param("search") String search,
            Pageable pageable);
}