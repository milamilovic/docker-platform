package com.dockerplatform.backend.service;

import com.dockerplatform.backend.models.Repository;
import com.dockerplatform.backend.models.User;
import com.dockerplatform.backend.models.enums.UserRole;
import com.dockerplatform.backend.repositories.RepositoryRepo;
import com.dockerplatform.backend.repositories.UserRepo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

//@ConditionalOnProperty(name="registry.jwt.enabled", havingValue="true", matchIfMissing=true)
@Service
public class RegistryTokenService {

    @Value("${registry.jwt.issuer}") private String issuer;
    @Value("${registry.jwt.keyId}") private String keyId;
    @Value("${registry.jwt.privateKeyPath}") private String privateKeyPath;

    @Autowired
    private RepositoryRepo repositoryRepo;

    @Autowired
    private UserRepo userRepo;

    private PrivateKey privateKey;

    public RegistryTokenService(RepositoryRepo repositoryRepo, UserRepo userRepo) {
        this.repositoryRepo = repositoryRepo;
        this.userRepo = userRepo;
    }

    @PostConstruct
    void init() throws Exception {
        String pem = Files.readString(Path.of(privateKeyPath));
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private record RepoScope(String type, String name, List<String> actions) {}
    private record RepoRef(String ownerUsername, String repoName) {}

    private Optional<RepoScope> parseScope(String scopeRaw) {
        if (scopeRaw == null || scopeRaw.isBlank()) return Optional.empty();

        String[] parts = scopeRaw.split(":", 3);
        String type = parts.length > 0 ? parts[0].trim() : "";
        String name = parts.length > 1 ? parts[1].trim() : "";
        List<String> actions = parts.length > 2
                ? Arrays.stream(parts[2].split(",")).map(String::trim).filter(s -> !s.isBlank()).toList()
                : List.of();

        if (type.isBlank() || name.isBlank()) return Optional.empty();
        return Optional.of(new RepoScope(type, name, actions));
    }

    private RepoRef parseRepoRefOrThrow(String fullName) {
        int slash = fullName.indexOf('/');
        if (slash <= 0 || slash == fullName.length() - 1) {
            throw new AccessDeniedException("Invalid repository name (expected owner/repo): " + fullName);
        }
        String owner = fullName.substring(0, slash).trim();
        String repo = fullName.substring(slash + 1).trim();
        if (owner.isBlank() || repo.isBlank()) {
            throw new AccessDeniedException("Invalid repository name (expected owner/repo): " + fullName);
        }
        return new RepoRef(owner, repo);
    }

    private static boolean isAdmin(User u) {
        return u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.SUPER_ADMIN;
    }

    private List<String> authorizeAndFilterActions(User requester, RepoScope s) {
        if (!"repository".equalsIgnoreCase(s.type())) return List.of();

        RepoRef ref = parseRepoRefOrThrow(s.name());

        Repository repo = repositoryRepo.findByOwnerUsernameAndName(ref.ownerUsername(), ref.repoName())
                .orElseThrow(() -> new AccessDeniedException("Repository not found: " + s.name()));

        boolean admin = isAdmin(requester);
        boolean owner = repo.getOwner() != null
                && repo.getOwner().getId() != null
                && requester.getId() != null
                && repo.getOwner().getId().equals(requester.getId());

        List<String> allowed = new ArrayList<>();

        for (String action : s.actions()) {
            switch (action) {
                case "pull" -> {
                    if (repo.isPublic() || owner || admin) {
                        allowed.add("pull");
                    }
                }
                case "push" -> {
                    if (repo.isOfficial()) {
                        if (admin) allowed.add("push");
                    } else {
                        if (owner || admin) allowed.add("push");
                    }
                }
                case "delete" -> {
                    if (owner || admin) allowed.add("delete");
                }
                default -> { /* ignore */ }
            }
        }

        if (s.actions().contains("pull") && !allowed.contains("pull")) {
            throw new AccessDeniedException("Pull forbidden: " + s.name());
        }
        if (s.actions().contains("push") && !allowed.contains("push")) {
            throw new AccessDeniedException("Push forbidden: " + s.name());
        }
        if (s.actions().contains("delete") && !allowed.contains("delete")) {
            throw new AccessDeniedException("Delete forbidden: " + s.name());
        }

        return allowed;
    }

    public String issue(String username, String service, String scopeRaw) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 15 * 60 * 1000);

        User requester = userRepo.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        List<Map<String, Object>> access = List.of();

        Optional<RepoScope> scopeOpt = parseScope(scopeRaw);
        if (scopeOpt.isPresent()) {
            RepoScope s = scopeOpt.get();
            List<String> allowedActions = authorizeAndFilterActions(requester, s);

            access = List.of(Map.of(
                    "type", s.type(),
                    "name", s.name(),
                    "actions", allowedActions
            ));
        }

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(issuer)
                .setSubject(username)
                .setAudience(service == null ? "local-registry" : service)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("access", access)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
