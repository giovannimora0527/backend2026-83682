package com.uniminuto.clinica.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import org.junit.jupiter.api.Test;

class FacturacionPermisosTest {
    @Test void aplicaLaMatrizFinanciera() {
        for(var rol:Rol.values()) {
            boolean tieneAcceso=rol==Rol.ADMIN || rol==Rol.RECEPCION;
            assertEquals(tieneAcceso,JwtAuthenticationFilter.permite(rol,"GET","/facturacion/facturas/42/pdf"));
            assertEquals(tieneAcceso,JwtAuthenticationFilter.permite(rol,"POST","/facturacion/atenciones/1/facturas"));
            assertEquals(tieneAcceso,JwtAuthenticationFilter.permite(rol,"POST","/facturacion/facturas/42/pagos"));
            assertEquals(rol==Rol.ADMIN,JwtAuthenticationFilter.permite(rol,"POST","/facturacion/facturas/42/anular"));
        }
        assertTrue(JwtAuthenticationFilter.permite(Rol.DESPACHO_MEDICO,"GET","/despacho/formulas"));
        assertFalse(JwtAuthenticationFilter.permite(Rol.MEDICO,"GET","/facturacion"));
    }
}
