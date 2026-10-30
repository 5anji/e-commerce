package com.ecommerce.server.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private static final String SECRET =
            "change-this-development-secret-key-to-a-long-random-value-123456789";

    @Test
    void acceptsTokenBeforeExpiration() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        String token = jwtService.generateToken(1L, "user@example.com", "USER");

        assertTrue(jwtService.isTokenValid(token, "user@example.com"));
    }

    @Test
    void rejectsTokenAfterExpiration() {
        JwtService jwtService = new JwtService(SECRET, -1_000);
        String token = jwtService.generateToken(1L, "user@example.com", "USER");

        assertFalse(jwtService.isTokenValid(token, "user@example.com"));
    }

    @Test
    void rejectsTokenForDifferentEmail() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        String token = jwtService.generateToken(1L, "user@example.com", "USER");

        assertFalse(jwtService.isTokenValid(token, "other@example.com"));
    }
}
