package com.dockerplatform.backend.security;

import com.dockerplatform.backend.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.issuer}")
    private String ISSUER;

    public static final long JWT_TOKEN_VALIDITY = 365L * 24L * 60L * 60L * 1000L;


    private Key getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Date getExpirationFormToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public String getUsernameFormToken(String token) {
        return getClaimFromToken(token,Claims::getSubject);
    }

    private <T> T getClaimFromToken(String token, Function<Claims,T> claimsResolver){
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    private Claims getAllClaimsFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationFormToken(token);
        return expiration.before(new Date());
    }
    public String generateToken(User user){
        return Jwts.builder()
                .setIssuer(ISSUER)
                .setSubject(user.getUsername())
                .claim("role",user.getRole())
                .claim("id", user.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey(),SignatureAlgorithm.HS512)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails){
        final String username = getUsernameFormToken(token);
        return (username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
        );
    }


}
