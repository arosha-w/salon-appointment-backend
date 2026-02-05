package com.saloon.saloon_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    // Secret key for signing the JWT, must be >= 32 characters for HS256
    private static final String SECRET = "saloon-secret-key-saloon-secret-key-1234";
    private static final long EXPIRY_MS = 1000L * 60 * 60; // 1 hour expiration time

    // Generate a secure key from the secret string
    private static SecretKey key() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // Generate JWT Token based on the subject (username or email)
    public static String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRY_MS);

        return Jwts.builder()
                .setSubject(subject)  // Set subject (usually the username or email)
                .setIssuedAt(now)     // Set token creation date
                .setExpiration(exp)   // Set token expiration date
                .signWith(key(), SignatureAlgorithm.HS256)      // Sign the token with the secret key
                .compact();           // Return the token
    }

    // Extract the subject (username/email) from the token
    public static String extractSubject(String token) {
        return Jwts.parserBuilder()  // Use the parserBuilder() for the latest JJWT versions
                .setSigningKey(key())  // Set the signing key to validate the token
                .build()
                .parseClaimsJws(token)  // Parse the claims from the JWT token
                .getBody()
                .getSubject();  // Get the subject (username or email) from the token
    }
}
