package com.dockerplatform.backend.service;

import com.dockerplatform.backend.dto.RepositoryDTO;
import com.dockerplatform.backend.dto.SearchCriteria;
import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.dockerplatform.backend.utils.RepositorySpecifications.*;

@Service
public class RepositoryService {

    @Autowired
    RepositoryRepo repositoryRepo;

    public Page<RepositoryDTO> findTopPulled(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repositoryRepo.findTopPulled(pageable);
    }

    public Page<RepositoryDTO> findTopStarred(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repositoryRepo.findTopStarred(pageable);
    }

    public Page<RepositoryDTO> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        SearchCriteria criteria = parse(q);
        Specification<Repository> spec = buildSpecification(criteria);

        Page<Repository> repoPage = repositoryRepo.findAll(spec, pageable);
        return repoPage.map(RepositoryDTO::from);
    }

    private SearchCriteria parse(String query) {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setGeneral(new ArrayList<>());

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
                        if ("official".equalsIgnoreCase(value)) criteria.setIsOfficial(true);
                        if ("verified".equalsIgnoreCase(value)) criteria.setIsVerified(true);
                        if ("sponsored".equalsIgnoreCase(value)) criteria.setIsSponsored(true);
                        break;
                }
            } else {
                criteria.getGeneral().add(token);
            }
        }

        return criteria;
    }

    private Specification<Repository> buildSpecification(SearchCriteria criteria) {
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

//        if (criteria.getOfficial() != null && criteria.getOfficial()) spec = spec.and(isOfficial());
//        if (criteria.getVerified() != null && criteria.getVerified()) spec = spec.and(isVerified());
//        if (criteria.getSponsored() != null && criteria.getSponsored()) spec = spec.and(isSponsored());

        return spec;
    }

}
