package com.uniminuto.clinica.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    public static String generarMD5(String cadena) {
        return generateMD5(cadena);
    }

    public static String generateMD5(String cadena) {
        if (cadena == null || cadena.isEmpty()) {
            return "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(cadena.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();

            for (byte b : bytes) {
                String part = Integer.toHexString(b & 0xFF);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar la clave MD5", e);
        }
    }
}
