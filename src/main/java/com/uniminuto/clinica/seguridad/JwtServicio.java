package com.uniminuto.clinica.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

/**
 * Emisión y verificación de tokens JWT firmados con HMAC-SHA256.
 *
 * <p>El token solo se usa como credencial al portador: además de la firma, el interceptor
 * verifica que exista una sesión activa y vigente en la tabla {@code session}, lo que permite
 * revocar sesiones (cierre de sesión o usuario inactivado).</p>
 */
@Component
public class JwtServicio {

    /** Codificador Base64 URL sin relleno, el formato que exige JWT. */
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    /** Clave secreta HMAC (variable JWT_SECRET). */
    private final byte[] secreto;

    /**
     * @param secreto clave secreta de firma (mínimo 32 caracteres).
     */
    public JwtServicio(@Value("${clinica.jwt.secret}") String secreto) {
        if (secreto == null || secreto.length() < 32) {
            throw new IllegalStateException("clinica.jwt.secret debe tener al menos 32 caracteres");
        }
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Emite un token JWT firmado con HS256.
     *
     * @param usuarioId identificador del usuario.
     * @param username nombre de usuario (claim sub).
     * @param perfil código del perfil.
     * @param emitido fecha de emisión en segundos epoch (iat).
     * @param expira fecha de expiración en segundos epoch (exp).
     * @return token JWT.
     */
    public String emitir(long usuarioId, String username, String perfil, long emitido, long expira) {
        String cabecera = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String cuerpo = "{\"sub\":\"" + escapar(username) + "\",\"uid\":" + usuarioId
                + ",\"perfil\":\"" + escapar(perfil) + "\",\"iat\":" + emitido + ",\"exp\":" + expira
                + ",\"jti\":\"" + UUID.randomUUID() + "\"}";
        String datos = cabecera + "." + B64.encodeToString(cuerpo.getBytes(StandardCharsets.UTF_8));
        return datos + "." + B64.encodeToString(firmar(datos));
    }

    /** Verifica estructura y firma del token. */
    public boolean firmaValida(String token) {
        if (token == null) {
            return false;
        }
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            return false;
        }
        byte[] esperada = firmar(partes[0] + "." + partes[1]);
        byte[] recibida;
        try {
            recibida = Base64.getUrlDecoder().decode(partes[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return MessageDigest.isEqual(esperada, recibida);
    }

    /**
     * Firma datos con HMAC-SHA256.
     *
     * @param datos cabecera y cuerpo del token codificados.
     * @return firma.
     */
    private byte[] firmar(String datos) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
            return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Escapa comillas y barras para incluir un texto en el JSON del token.
     *
     * @param texto texto original.
     * @return texto escapado.
     */
    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
