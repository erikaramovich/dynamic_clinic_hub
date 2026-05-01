package com.miro.project.security;

import com.miro.project.model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    // Pulls from application.yaml. We provide a default fallback here for testing.
    @Value("${app.jwt.secret:supersecretkeythatisverylongandsecureforjwtsigning123!}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:3600000}") // Default 1 hour
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String name, Role role) {
        return Jwts.builder()
                .subject(name)
                .claim("role", "ROLE_" + role.toString()) // Important: Embed the role into the JWT payload!
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }
}