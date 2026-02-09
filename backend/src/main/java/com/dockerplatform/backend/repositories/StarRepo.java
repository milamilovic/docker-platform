package com.dockerplatform.backend.repositories;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.Star;
import com.dockerplatform.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface StarRepo extends JpaRepository<Star, UUID> {
    Optional<Star> findByUserIdAndRepositoryId(UUID userId, UUID repositoryId);

    void deleteByUserIdAndRepositoryId(UUID userId, UUID repositoryId);

    @Query("""
        select r
        from Star s
        join s.repository r
        where s.user.id = :userId
          and (:search is null or :search = '' or lower(r.name) like lower(concat('%', :search, '%')))
    """)
    Page<Repository> findStarredRepositoriesByUser(UUID userId, String search, Pageable pageable);

    boolean existsByUserAndRepository(User currentUser, Repository repository);
}
