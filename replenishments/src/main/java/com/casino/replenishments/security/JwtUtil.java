package com.casino.replenishments.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    public Long extractIdFromAccessToken(String token){
        return extractAccessClaim(token, claims -> Long.parseLong(claims.getSubject()));
    }

    private Claims extractAccessAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(token).getBody();
    }

    public <T> T extractAccessClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAccessAllClaims(token);
        return claimsResolver.apply(claims);
    }
}
