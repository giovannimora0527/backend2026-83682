package com.uniminuto.clinica.config;

import static org.junit.jupiter.api.Assertions.*;

import com.uniminuto.clinica.service.IdentidadService.Rol;
import org.junit.jupiter.api.Test;

class Fase5PermisosTest {
    @Test void soloDespachoPuedeConfirmarYRevertir() {
        for(var rol:Rol.values()) {
            assertEquals(rol==Rol.DESPACHO_MEDICO,JwtAuthenticationFilter.permite(rol,"GET","/despacho/formulas"));
            assertEquals(rol==Rol.DESPACHO_MEDICO,JwtAuthenticationFilter.permite(rol,"POST","/despacho/entregas/1/confirmar"));
            assertEquals(rol==Rol.DESPACHO_MEDICO,JwtAuthenticationFilter.permite(rol,"POST","/despacho/entregas/1/revertir"));
        }
    }

    @Test void despachoMantieneInventarioDeSoloLectura() {
        Rol rol=Rol.DESPACHO_MEDICO;
        assertTrue(JwtAuthenticationFilter.permite(rol,"GET","/inventario/productos/12/lotes"));
        assertFalse(JwtAuthenticationFilter.permite(rol,"PUT","/inventario/productos/12"));
        assertFalse(JwtAuthenticationFilter.permite(rol,"POST","/inventario/productos/12/lotes/1/movimientos"));
        assertFalse(JwtAuthenticationFilter.permite(rol,"PUT","/atencion/citas/1/formulas/2"));
        assertFalse(JwtAuthenticationFilter.permite(rol,"GET","/historia-medica/listar"));
    }
}
