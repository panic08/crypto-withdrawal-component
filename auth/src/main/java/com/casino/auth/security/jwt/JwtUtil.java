package com.casino.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    public Mono<String> generateToken(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();

        return Mono.defer(() -> createToken(claims, userDetails.getUsername()));
    }

    public Mono<Date> extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public Mono<String> extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private Mono<Claims> extractAllClaims(String token) {
        return Mono.fromCallable(() ->
                Jwts.parserBuilder().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(token).getBody());
    }

    public <T> Mono<T> extractClaim(String token, Function<Claims, T> claimsResolver){
        return extractAllClaims(token)
                .flatMap(claims -> Mono.fromCallable(() -> claimsResolver.apply(claims)));
    }

    private Mono<String> createToken(Map<String, Object> claims, String subject){
        return Mono.fromCallable(() -> {
            Date dateNow = new Date();
            Date expirationDate = new Date(dateNow.getTime() + 1000 * 60 * 60 * 24 * 23);

            byte[] signingKeyBytes = jwtSecret.getBytes();

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(dateNow)
                    .setExpiration(expirationDate)
                    .signWith(Keys.hmacShaKeyFor(signingKeyBytes), SignatureAlgorithm.HS256)
                    .compact();
        });
    }

    public Mono<Boolean> isTokenExpired(String token){
        return extractExpiration(token).flatMap(date -> Mono.fromCallable(() -> date.before(new Date())));
    }

    public Mono<Boolean> isTokenValid(String token){
        return Mono.fromCallable(() -> {
            try {
                Jwts.parserBuilder().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(token);
                return true;
            }catch (Exception ignored){
                return false;
            }
        });
    }
}
