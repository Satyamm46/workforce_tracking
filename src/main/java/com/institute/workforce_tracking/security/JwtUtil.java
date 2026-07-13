package com.institute.workforce_tracking.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;

/**
 * Central holder for JWT signing material and token lifetimes.
 *
 * <p><strong>Placeholder scope (foundation milestone):</strong> this class
 * currently loads and validates the JWT configuration and derives the signing
 * key once, at startup. Token <em>generation</em> and <em>validation</em>
 * methods are intentionally deferred to the authentication milestone, where
 * they will be added to this same class — no surrounding change required.</p>
 *
 * <p>Validating the secret at construction time means a misconfigured secret
 * fails the application fast, at boot, rather than at the first login attempt.</p>
 */
@Component
public class JwtUtil {

    /** Minimum secret length (bytes) for a secure HMAC-SHA-256 signing key. */
    private static final int MIN_SECRET_LENGTH_BYTES = 32;

    /**
     * The signing key, derived once from the configured secret.
     * Prepared here for the token operations added in the auth milestone.
     */
    private final SecretKey signingKey;

    /** Access-token lifetime in milliseconds. */
    @Getter
    private final long accessTokenExpirationMs;

    /** Refresh-token lifetime in milliseconds. */
    @Getter
    private final long refreshTokenExpirationMs;

    /**
     * @param secret                    signing secret (min 256 bits / 32 bytes)
     * @param accessTokenExpirationMs   access-token lifetime in ms
     * @param refreshTokenExpirationMs  refresh-token lifetime in ms
     */
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
}