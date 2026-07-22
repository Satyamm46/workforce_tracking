package com.institute.workforce_tracking.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

/**
 * Creates and verifies JSON Web Tokens (JWTs) for stateless authentication.
 *
 * <p>On successful login an access token is generated carrying the user's
 * email (as the subject) and role (as a custom claim), signed with an HMAC-SHA
 * key derived from the configured secret. On every subsequent request the
 * {@code JwtAuthenticationFilter} uses this class to verify the token's
 * signature and expiry and to read those claims — so the server can trust the
 * caller's identity without any server-side session.</p>
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /** Claim key under which the user's role is stored inside the token. */
    private static final String CLAIM_ROLE = "role";

    /** Minimum secret length (bytes) for a secure HMAC-SHA signing key. */
    private static final int MIN_SECRET_LENGTH_BYTES = 32;

    /** Signing key, derived once from the configured secret. */
    private final SecretKey signingKey;

    /** Access-token lifetime in milliseconds. */
    @Getter
    private final long accessTokenExpirationMs;

    /** Refresh-token lifetime in ms — reserved for a future refresh-token flow. */
    @Getter
    private final long refreshTokenExpirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenExpirationMs) {

        if (secret == null
                || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least " + MIN_SECRET_LENGTH_BYTES
                            + " bytes (256 bits) to safely sign JWTs.");
        }

        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Generates a signed access token for an authenticated user.
     *
     * @param email the user's email (becomes the token subject)
     * @param role  the user's role name (stored as a custom claim)
     * @return a compact, URL-safe, signed JWT string
     */
    public String generateAccessToken(String email, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_ROLE, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the email (subject) from a token.
     *
     * @param token the JWT to read
     * @return the subject claim (the user's email)
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the role claim from a token.
     *
     * @param token the JWT to read
     * @return the role name stored in the token
     */
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    /**
     * Verifies a token's signature and expiry.
     *
     * @param token the JWT to validate
     * @return {@code true} if the token is well-formed, correctly signed, and
     *         not expired; {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Parses and verifies a token, returning its claims.
     * Throws if the signature is invalid, the token is malformed, or expired.
     *
     * @param token the JWT to parse
     * @return the verified claims payload
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}