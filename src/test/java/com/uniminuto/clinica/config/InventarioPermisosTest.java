package com.uniminuto.clinica.config;

import org.junit.jupiter.api.Test;
import com.uniminuto.clinica.service.IdentidadService.Rol;

import static org.junit.jupiter.api.Assertions.*;

class InventarioPermisosTest {
    @Test void administraSoloAdminYLecturaLimitadaParaDespachoYMedico() {
        assertTrue(JwtAuthenticationFilter.permite(Rol.ADMIN,"POST","/inventario/importaciones"));
        assertTrue(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"GET","/inventario/productos"));
        assertTrue(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"GET","/inventario/productos/1/lotes"));
        assertFalse(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"PUT","/inventario/productos/1"));
        assertFalse(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"POST","/inventario/productos/1/lotes/2/movimientos"));
        assertFalse(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"GET","/inventario/importaciones"));
        assertFalse(JwtAuthenticationFilter.permite(Rol.RECEPCION,"GET","/inventario/productos"));
        assertTrue(JwtAuthenticationFilter.permite(Rol.MEDICO,"GET","/inventario/productos"));
        assertFalse(JwtAuthenticationFilter.permite(Rol.MEDICO,"POST","/inventario/productos/1/lotes"));
    }
}
