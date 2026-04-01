package com.solutionti.usuarios.security;

import com.solutionti.usuarios.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final String TOKEN_TYPE = "type";
    private static final String ACCESS_TOKEN = "access";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-expiration:${jwt.expiration:900000}}")
    private long accessTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", usuario.getTipo().name());
        claims.put("nome", usuario.getNome());
        claims.put(TOKEN_TYPE, ACCESS_TOKEN);

        Date now = new Date();
        return Jwts.builder()
            .claims(claims)
            .subject(usuario.getId().toString())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessTokenExpiration))
            .signWith(getSigningKey(), Jwts.SIG.HS256)
            .compact();
    }

    public String getUserIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
            return ACCESS_TOKEN.equals(claims.get(TOKEN_TYPE, String.class));
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT nao suportado: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Assinatura JWT invalida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Claims JWT vazio ou nulo: {}", e.getMessage());
        }
        return false;
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
