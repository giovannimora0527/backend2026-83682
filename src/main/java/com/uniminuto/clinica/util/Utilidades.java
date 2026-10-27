package com.uniminuto.clinica.util;

import com.uniminuto.clinica.entity.OtpToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class Utilidades {
    /**
     * Genera un token OTP (One-Time Password) de 6 caracteres alfanuméricos.
     * @return Token OTP generado.
     */
    private static final String CARACTERES_OTP = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    /**
     * Longitud del token OTP.
     */
    private static final int LONGITUD_OTP = 6;
    /**
     * Generador de números aleatorios seguro para la generación de tokens.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generarTokenOtp() {
        StringBuilder token = new StringBuilder(LONGITUD_OTP);
        for (int i = 0; i < LONGITUD_OTP; i++) {
            token.append(CARACTERES_OTP.charAt(RANDOM.nextInt(CARACTERES_OTP.length())));
        }
        return token.toString();
    }

    public static String generarJwtToken(OtpToken otpToken, String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Debe configurar JWT_SECRET en Base64 con al menos 64 bytes aleatorios.");
        }
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return generarJwtToken(otpToken, key);
    }

    public static String generarJwtToken(OtpToken otpToken, SecretKey key) {
        if (otpToken == null || otpToken.getUsuarioId() == null) {
            throw new IllegalArgumentException("El token OTP debe tener un usuario asociado.");
        }
        Instant now = Instant.now();
        
        return Jwts.builder()
                .subject(otpToken.getUsuarioId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
