package com.uniminuto.clinica.seguridad;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeguridadTest {

    @Test
    void hashPbkdf2ConSalAleatoria() {
        String a = ClaveUtil.hash("Clave*2026");
        String b = ClaveUtil.hash("Clave*2026");
        assertNotEquals(a, b);
        assertTrue(ClaveUtil.verificar("Clave*2026", a));
        assertFalse(ClaveUtil.verificar("otra", a));
    }

    @Test
    void aceptaHashMd5Heredado() {
        String md5 = "5f4dcc3b5aa765d61d8327deb882cf99"; // "password"
        assertTrue(ClaveUtil.esHeredado(md5));
        assertTrue(ClaveUtil.verificar("password", md5));
        assertFalse(ClaveUtil.verificar("Password", md5));
    }

    @Test
    void jwtDetectaManipulacion() {
        JwtServicio jwt = new JwtServicio("clave-de-pruebas-con-mas-de-32-caracteres-123");
        String token = jwt.emitir(1, "admin", "ADMINISTRADOR", 1, 2);
        assertTrue(jwt.firmaValida(token));
        String[] partes = token.split("\\.");
        assertFalse(jwt.firmaValida(partes[0] + "." + partes[1] + "x." + partes[2]));
        assertFalse(jwt.firmaValida("no-es-un-token"));
    }

    @Test
    void normalizaNombresDePerfil() {
        assertEquals("ADMINISTRADOR", Perfiles.codigo("Administrador"));
        assertEquals("MEDICO", Perfiles.codigo(" Médico "));
        assertEquals("DESPACHO_MEDICO", Perfiles.codigo("Despacho médico"));
        assertEquals("RECEPCION", Perfiles.codigo("Recepción"));
    }
}
