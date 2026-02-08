package com.dockerplatform.backend.utils;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.enums.BadgeType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

public class RepositorySpecifications {

    public static Specification<Repository> isPublic() {
        return (root, query, cb) -> cb.isTrue(root.get("isPublic"));
    }

    public static Specification<Repository> repoNameLike(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Repository> ownerLike(String ownerUsername) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("ownerUsername")), "%" + ownerUsername.toLowerCase() + "%");
    }

    public static Specification<Repository> descriptionLike(String desc) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("description")), "%" + desc.toLowerCase() + "%");
    }

    public static Specification<Repository> isOfficial() {
        return (root, query, cb) -> cb.isTrue(root.get("isOfficial"));
    }

    public static Specification<Repository> isVerified() {
        return (root, query, cb) -> cb.isTrue(root.get("isVerified"));
    }

    public static Specification<Repository> isSponsored() {
        return (root, query, cb) -> cb.isTrue(root.get("isSponsored"));
    }

    public static Specification<Repository> hasAnyBadge(Set<BadgeType> badges) {
        return (root, query, cb) ->
                root.get("badge").in(badges);
    }
}
