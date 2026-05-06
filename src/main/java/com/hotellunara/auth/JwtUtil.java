package com.hotellunara.auth;

import com.hotellunara.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final Key signingKey;
    private final long expiration;
    private final long refreshExpiration;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration}") long expiration,
                   @Value("${app.jwt.refresh-expiration}") long refreshExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = baseClaims(user);
        claims.put("type", "ACCESS");
        return buildToken(claims, user.getEmail(), expiration);
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = baseClaims(user);
        claims.put("type", "REFRESH");
        return buildToken(claims, user.getEmail(), refreshExpiration);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isRefreshToken(String token) {
        return "REFRESH".equals(extractAllClaims(token).get("type", String.class));
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public long getExpiration() {
        return expiration;
    }

    private Map<String, Object> baseClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        return claims;
    }

    private String buildToken(Map<String, Object> claims, String subject, long ttlMillis) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + ttlMillis);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
