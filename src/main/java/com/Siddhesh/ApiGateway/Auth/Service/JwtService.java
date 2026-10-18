package com.Siddhesh.ApiGateway.Auth.Service;

import com.Siddhesh.ApiGateway.Auth.Entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.*;

@Service
public class JwtService {

    @Value("${jwt.secret:}")
    private String secretKey;

    public String generateToken(String username, Role role){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1000 * 60)) // 60 minutes
                .claim("Role", role.name())
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey(){
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. Set the JWT_SECRET environment variable.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        Claims claims = getAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        if (token == null || userDetails == null) {
            return false;
        }
        try {
            String username = extractUsername(token);
            return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaims(token);
            return claims == null || claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Claims getAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
