package com.uniminuto.clinica.seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Hash de contraseñas con PBKDF2-HMAC-SHA256 y sal aleatoria.
 *
 * <p>Formato almacenado: {@code pbkdf2$<iteraciones>$<sal base64>$<hash base64>}.
 * Por compatibilidad con la versión anterior del sistema se aceptan hashes MD5 heredados;
 * al iniciar sesión con uno de ellos el servicio de autenticación lo reemplaza por PBKDF2.</p>
 */
public final class ClaveUtil {

    /** Iteraciones de PBKDF2 (costo de cálculo frente a ataques de fuerza bruta). */
    private static final int ITERACIONES = 120_000;
    /** Generador criptográfico para las sales. */
    private static final SecureRandom ALEATORIO = new SecureRandom();

    /** Clase de utilidades: no se instancia. */
    private ClaveUtil() {
    }

    /**
     * Calcula el hash PBKDF2-HMAC-SHA256 de una contraseña con sal aleatoria.
     *
     * @param clave contraseña en texto plano.
     * @return texto con el formato pbkdf2$iteraciones$sal$hash.
     */
    public static String hash(String clave) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        byte[] hash = pbkdf2(clave, sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifica una contraseña contra el hash almacenado (PBKDF2 o MD5 heredado) en tiempo constante.
     *
     * @param clave contraseña ingresada.
     * @param almacenado hash guardado en la base de datos.
     * @return true si la contraseña es correcta.
     */
    public static boolean verificar(String clave, String almacenado) {
        if (clave == null || almacenado == null || almacenado.isBlank()) {
            return false;
        }
        if (esHeredado(almacenado)) {
            byte[] md5 = digest("MD5", clave);
            return MessageDigest.isEqual(HexFormat.of().formatHex(md5).getBytes(StandardCharsets.US_ASCII),
                    almacenado.toLowerCase().getBytes(StandardCharsets.US_ASCII));
        }
        String[] partes = almacenado.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        byte[] calculado = pbkdf2(clave, sal, Integer.parseInt(partes[1]));
        return MessageDigest.isEqual(esperado, calculado);
    }

    /** Hash MD5 heredado (32 caracteres hexadecimales). */
    public static boolean esHeredado(String almacenado) {
        return almacenado != null && almacenado.matches("[0-9a-fA-F]{32}");
    }

    /** SHA-256 en hexadecimal; se usa para guardar los tokens de sesión sin exponerlos. */
    public static String sha256(String texto) {
        return HexFormat.of().formatHex(digest("SHA-256", texto));
    }

    /**
     * Deriva la clave con PBKDF2-HMAC-SHA256.
     *
     * @param clave contraseña.
     * @param sal sal aleatoria.
     * @param iteraciones número de iteraciones.
     * @return hash de 256 bits.
     */
    private static byte[] pbkdf2(String clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No fue posible calcular el hash de la contraseña", e);
        }
    }

    /**
     * Calcula un resumen criptográfico.
     *
     * @param algoritmo nombre del algoritmo (MD5, SHA-256).
     * @param texto texto a resumir en UTF-8.
     * @return bytes del resumen.
     */
    private static byte[] digest(String algoritmo, String texto) {
        try {
            return MessageDigest.getInstance(algoritmo).digest(texto.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
