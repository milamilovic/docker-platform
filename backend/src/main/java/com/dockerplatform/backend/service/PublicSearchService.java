package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.RepositorySearchDTO;
import com.dockerplatform.backend.dto.SearchCriteria;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.enums.BadgeType;
import com.dockerplatform.backend.repositories.PublicRepositoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;

import static com.dockerplatform.backend.utils.RepositorySpecifications.*;
import static com.dockerplatform.backend.utils.RepositorySpecifications.descriptionLike;
import static com.dockerplatform.backend.utils.RepositorySpecifications.hasAnyBadge;
import static com.dockerplatform.backend.utils.RepositorySpecifications.ownerLike;
import static com.dockerplatform.backend.utils.RepositorySpecifications.repoNameLike;

@Service
public class PublicSearchService {

    @Autowired
    PublicRepositoryRepo publicRepositoryRepo;

    public Page<RepositorySearchDTO> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        SearchCriteria criteria = parse(q);
        Specification<Repository> spec = buildSpecification(criteria);

        Page<Repository> repoPage = publicRepositoryRepo.findAll(spec, pageable);
        return repoPage.map(RepositorySearchDTO::from);
    }

    SearchCriteria parse(String query) {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setGeneral(new ArrayList<>());
        criteria.setBadges(new HashSet<>());

        if (query == null || query.isBlank()) return criteria;

        for (String token : query.split("\\s+")) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if (token.contains(":")) {
                String[] parts = token.split(":", 2);
                String key = parts[0].toLowerCase();
                String value = parts[1];

                switch (key) {
                    case "repo":
                        criteria.setRepo(value);
                        break;
                    case "owner":
                        criteria.setOwner(value);
                        break;
                    case "desc":
                        criteria.setDescription(value);
                        break;
                    case "is":
                        switch (value.toLowerCase()) {
                            case "official" ->
                                    criteria.getBadges().add(BadgeType.DOCKER_OFFICIAL_IMAGE);
                            case "verified" ->
                                    criteria.getBadges().add(BadgeType.VERIFIED_PUBLISHER);
                            case "sponsored" ->
                                    criteria.getBadges().add(BadgeType.SPONSORED_OSS);
                        }
                        break;
                }
            } else {
                criteria.getGeneral().add(token);
            }
        }

        return criteria;
    }

    Specification<Repository> buildSpecification(SearchCriteria criteria) {
        Specification<Repository> spec = isPublic();

        if (criteria.getRepo() != null)
            spec = spec.and(repoNameLike(criteria.getRepo()));

        if (criteria.getOwner() != null)
            spec = spec.and(ownerLike(criteria.getOwner()));

        if (criteria.getDescription() != null)
            spec = spec.and(descriptionLike(criteria.getDescription()));

        if (!criteria.getGeneral().isEmpty()) {
            Specification<Repository> generalSpec = null;
            for (String term : criteria.getGeneral()) {
                Specification<Repository> termSpec = repoNameLike(term)
                        .or(descriptionLike(term))
                        .or(ownerLike(term));
                generalSpec = (generalSpec == null) ? termSpec : generalSpec.and(termSpec);
            }
            spec = spec.and(generalSpec);
        }

        if (!criteria.getBadges().isEmpty()) {
            spec = spec.and(hasAnyBadge(criteria.getBadges()));
        }

        return spec;
    }
}
