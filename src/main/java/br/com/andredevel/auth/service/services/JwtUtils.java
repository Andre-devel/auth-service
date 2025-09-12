package br.com.andredevel.auth.service.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtUtils {
    
    @Value("${jwt.secret}") 
    private String secret;  
    
    @Value("${jwt.expiration}") 
    private String expiration;     
    
    private Key key;

    @PostConstruct
    public void initKey() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    } 
    
    public Claims parseToken(String token) {
        return io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();
    }   
    
    public Date getExpirationDate(String token) {   
        return parseToken(token).getExpiration();
    }
    
    public String generateToken(String userId, String role, String tokenType) {
        Map<String, String> claims = Map.of("userId", userId, "role", role);
        long expirationMillis = "ACCESS".equalsIgnoreCase(tokenType) 
                ? Long.parseLong(expiration) * 1000 
                : Long.parseLong(expiration) * 1000 * 5; 
        
        final Date now = new Date();    
        final Date expiryDate = new Date(now.getTime() + expirationMillis); 
        
        return io.jsonwebtoken.Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.get("userId"))
                .setIssuedAt(now)   
                .setExpiration(expiryDate)  
                .signWith(key)
                .compact(); 
    }

    public boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }
}
