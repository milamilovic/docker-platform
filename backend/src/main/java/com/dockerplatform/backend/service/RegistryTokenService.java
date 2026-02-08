package com.dockerplatform.backend.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@Service
public class RegistryTokenService {

    @Value("${registry.jwt.issuer}") private String issuer;
    private String ISSUER;
    @Value("${registry.jwt.keyId}") private String keyId;
    @Value("${registry.jwt.privateKeyPath}") private String privateKeyPath;

    private PrivateKey privateKey;

    @PostConstruct
    void init() throws Exception {
        String pem = Files.readString(Path.of(privateKeyPath));
        pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    public String issue(String subject, String service, String scopeRaw) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 15 * 60 * 1000);

        // Minimalno: vrati tačno scope koji je tražen (bez provere)
        // Registry očekuje "access": [{type,name,actions}]
        List<Map<String,Object>> access = List.of();

        if (scopeRaw != null && !scopeRaw.isBlank()) {
            // repository:stefan/repoA:pull,push
            String[] parts = scopeRaw.split(":", 3);
            String type = parts.length > 0 ? parts[0] : "";
            String name = parts.length > 1 ? parts[1] : "";
            List<String> actions = parts.length > 2
                    ? Arrays.stream(parts[2].split(",")).filter(s -> !s.isBlank()).toList()
                    : List.of();
            access = List.of(Map.of("type", type, "name", name, "actions", actions));
        }

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(issuer)
                .setSubject(subject)
                .setAudience(service == null ? "local-registry" : service)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("access", access)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
