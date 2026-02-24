package com.debmalya.urlShortener.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.debmalya.urlShortener.service.UserDetailsImpl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.io.Decoders;
import java.security.Key;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;

import java.util.stream.Collectors;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    public String getJwtFromRequest(HttpServletRequest request) {
        // Logic to extract JWT from the request header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Key getKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateJwtToken(UserDetailsImpl userDetails) {
        String username = userDetails.getUsername();
        String roles= userDetails.getAuthorities().stream()
                .map( authority -> authority.getAuthority())
                .collect(Collectors.joining(","));
                
        return Jwts.builder()
                   .subject(username)
                   .claim("roles", roles)
                   .issuedAt(new Date())
                   .expiration(new Date(new Date().getTime() + jwtExpirationMs)) // 24 hours expiration
                   .signWith(getKey())
                   .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                    .verifyWith((SecretKey) getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT expired");
        } catch (SecurityException e) {
            System.out.println("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            System.out.println("Malformed JWT token");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claims string is empty");
        } catch (Exception e) {
            System.out.println("JWT validation error");
        }

        return false;
    }

}
