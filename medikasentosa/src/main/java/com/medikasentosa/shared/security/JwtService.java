package com.medikasentosa.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // Buat token saat berhasil login
    public String generateToken(String email) {
        return Jwts.builder().subject(email).issuedAt(new Date()) // berdasarkan email dan kapan dibuat
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // Kedaluarsa
                .signWith(getSignKey()) // TTD rahasia
                .compact();
    }

    // Get data email dari token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Cek token apakah token cocok dengan user dan belum kedaluarsa
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isExpired(token);
    }

    // Helper
    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser().verifyWith(getSignKey()).build()
                .parseSignedClaims(token).getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
