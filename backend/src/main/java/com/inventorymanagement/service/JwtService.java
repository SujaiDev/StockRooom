package com.inventorymanagement.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret:}") String configuredSecret,
                      @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            this.signingKey = Jwts.SIG.HS256.key().build();
            log.warn("JWT_SECRET is not set; using an ephemeral local-only JWT key. Tokens will expire when the API restarts.");
        } else {
            byte[] secretBytes;
            try {
                secretBytes = Decoders.BASE64.decode(configuredSecret);
            } catch (IllegalArgumentException exception) {
                secretBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
            }
            if (secretBytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET must provide at least 256 bits of key material");
            }
            this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        }
        if (expirationMs < 60_000) throw new IllegalArgumentException("JWT expiration must be at least 60 seconds");
        this.expirationMs = expirationMs;
    }

    public String createToken(String email) {
        Instant now = Instant.now();
        return Jwts.builder().subject(email).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs))).signWith(signingKey).compact();
    }

    public String extractEmail(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token, String expectedEmail) {
        Claims claims = claims(token);
        return expectedEmail.equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}