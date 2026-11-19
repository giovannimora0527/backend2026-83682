package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey claveFirma;
    private final JwtParser parser;

    public JwtService(@Value("${app.jwt.secret}") String claveBase64) {
        try {
            claveFirma = Keys.hmacShaKeyFor(Decoders.BASE64.decode(claveBase64));
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("JWT_SECRET debe ser Base64 y contener al menos 32 bytes.");
        }
        parser = Jwts.parser().verifyWith(claveFirma).build();
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("usuarioId", usuario.getId())
                .claim("perfilId", usuario.getPerfilId())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(2, ChronoUnit.HOURS)))
                .signWith(claveFirma)
                .compact();
    }

    public Claims leerToken(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null) {
            throw new IllegalArgumentException("El token requiere vencimiento.");
        }
        return claims;
    }
}
