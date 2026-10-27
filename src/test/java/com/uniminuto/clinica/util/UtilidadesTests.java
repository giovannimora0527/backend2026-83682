package com.uniminuto.clinica.util;

import com.uniminuto.clinica.entity.OtpToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilidadesTests {

    @Test
    void generarJwtTokenDebeFirmarConHs512YExpirarEnUnaHora() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);

        String token = Utilidades.generarJwtToken(otpToken, key);
        var jwt = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        Claims claims = jwt.getPayload();

        assertEquals("HS512", jwt.getHeader().getAlgorithm());
        assertEquals("42", claims.getSubject());
        assertEquals(3_600_000L,
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void generarJwtTokenDebeRechazarUsuarioNulo() {
        SecretKey key = Jwts.SIG.HS512.key().build();

        assertThrows(IllegalArgumentException.class,
                () -> Utilidades.generarJwtToken(null, key));
        assertThrows(IllegalArgumentException.class,
                () -> Utilidades.generarJwtToken(new OtpToken(), key));
    }

    @Test
    void generarJwtTokenDebeRechazarClaveInsuficienteParaHs512() {
        SecretKey key = Jwts.SIG.HS256.key().build();
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);

        assertThrows(WeakKeyException.class,
                () -> Utilidades.generarJwtToken(otpToken, key));
    }

    @Test
    void generarJwtTokenDebeUsarSecretoConfiguradoEnBase64() {
        SecretKey key = Jwts.SIG.HS512.key().build();
        OtpToken otpToken = new OtpToken();
        otpToken.setUsuarioId(42L);

        String token = Utilidades.generarJwtToken(
                otpToken, Encoders.BASE64.encode(key.getEncoded()));

        assertEquals("42", Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject());
    }

    @Test
    void generarJwtTokenDebeRechazarSecretoVacio() {
        assertThrows(IllegalStateException.class,
                () -> Utilidades.generarJwtToken(new OtpToken(), ""));
    }

    @Test
    void generarTokenOtpDebeRetornarSeisCaracteresAlfanumericos() {
        for (int i = 0; i < 1000; i++) {
            String token = Utilidades.generarTokenOtp();

            assertEquals(6, token.length());
            assertTrue(token.matches("[A-Z0-9]{6}"));
        }
    }
}
