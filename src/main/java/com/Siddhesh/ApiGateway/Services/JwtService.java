package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Entities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class JwtService {

    private String SecretKey = "";

    public JwtService(){
        try {
            KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey key = generator.generateKey();
            SecretKey = Base64.getEncoder().encodeToString(key.getEncoded());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(String username, Role role){
        Map<String, Objects> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 1000 * 15))
                .and()
                .claim("Role", role)
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey(){
        byte[] byteKey = Decoders.BASE64.decode(SecretKey);
        return Keys.hmacShaKeyFor(byteKey);
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return getAllClaims(token).getExpiration().before(new Date());
    }

    private Claims getAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
