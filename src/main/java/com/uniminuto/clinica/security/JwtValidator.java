package com.uniminuto.clinica.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/** Valida firma y expiracion del JWT. Impide arrancar con una clave insuficiente para HS512. */
@Component
public class JwtValidator {

    private static final int BYTES_MINIMOS = 64;

    private final SecretKey key;

    public JwtValidator(@Value("${JWT_SECRET}") String secreto) {
        byte[] bytes = Decoders.BASE64.decode(secreto);
        if (bytes.length < BYTES_MINIMOS) {
            throw new IllegalStateException("JWT_SECRET debe ser Base64 de al menos 64 bytes aleatorios.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public Claims validar(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
