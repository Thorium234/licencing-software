package com.thorium.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService - Service for JWT token generation and validation.
 *
 * Handles:
 * - JWT token generation
 * - Token validation
 * - Claims extraction
 * - Token refresh
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@Service
public class JwtService {

    @Value("${thorium.jwt.secret}")
    private String jwtSecret;

    @Value("${thorium.jwt.access-expiration}")
    private long accessTokenExpiration;

    @Value("${thorium.jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    /**
     * Generate a JWT token for the given subject and token type.
     *
     * @param subject    The subject (user ID)
     * @param tokenType The token type (ACCESS or REFRESH)
     * @return The generated JWT token
     */
    public String generateToken(String subject, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", tokenType);

        long expiration = "REFRESH".equals(tokenType) ? refreshTokenExpiration : accessTokenExpiration;

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the subject from a JWT token.
     *
     * @param token The JWT token
     * @return The subject (user ID)
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from a JWT token.
     *
     * @param token The JWT token
     * @return The expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract a specific claim from a JWT token.
     *
     * @param token          The JWT token
     * @param claimsResolver The claims resolver function
     * @param <T>            The type of the claim
     * @return The claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from a JWT token.
     *
     * @param token The JWT token
     * @return All claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if a JWT token is expired.
     *
     * @param token The JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate a JWT token.
     *
     * @param token The JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate a JWT token for a specific user.
     *
     * @param token       The JWT token
     * @param userId      The expected user ID
     * @param tokenType   The expected token type
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token, String userId, String tokenType) {
        try {
            final String extractedSubject = extractSubject(token);
            final String extractedType = extractClaim(token, claims -> claims.get("type", String.class));

            return (extractedSubject.equals(userId) || extractedSubject.equals(userId))
                   && extractedType.equals(tokenType)
                   && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the signing key for JWT operations.
     *
     * @return The secret key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get the access token expiration time in milliseconds.
     *
     * @return Access token expiration time
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Get the refresh token expiration time in milliseconds.
     *
     * @return Refresh token expiration time
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
