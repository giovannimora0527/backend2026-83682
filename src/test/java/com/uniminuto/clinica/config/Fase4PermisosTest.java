package com.uniminuto.clinica.config;

import com.uniminuto.clinica.service.IdentidadService.Rol;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Fase4PermisosTest {
    @Test void tarifarioYClinicaTienenPermisosSeparados() {
        String precio="/tarifario/servicios/1/tarifas", consulta="/atencion/citas/1";
        assertTrue(JwtAuthenticationFilter.permite(Rol.ADMIN,"POST",precio));
        assertFalse(JwtAuthenticationFilter.permite(Rol.MEDICO,"POST",precio));
        assertFalse(JwtAuthenticationFilter.permite(Rol.ADMIN,"GET",consulta));
        assertFalse(JwtAuthenticationFilter.permite(Rol.RECEPCION,"GET",consulta));
        assertFalse(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"POST","/atencion/formulas/2/confirmar"));
        assertTrue(JwtAuthenticationFilter.permite(Rol.MEDICO,"PUT",consulta));
        assertFalse(JwtAuthenticationFilter.permite(Rol.ADMIN,"POST","/formula-medica/crear"));
        assertTrue(JwtAuthenticationFilter.permite(Rol.MEDICO,"POST","/formula-medica/crear"));
        assertTrue(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"GET","/inventario/productos"));
        assertTrue(JwtAuthenticationFilter.permite(Rol.RECEPCION,"GET","/cita/listar"));
    }
}
