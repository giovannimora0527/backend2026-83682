package com.uniminuto.clinica.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import org.junit.jupiter.api.Test;

class ReportesPermisosTest {
    @Test void limitaRecepcionAlMismoReporteEnLosTresFormatos() {
        for(var rol:Rol.values()) {
            for(String formato:new String[]{"","/pdf","/excel"}) {
                assertEquals(rol==Rol.ADMIN || rol==Rol.RECEPCION,
                        JwtAuthenticationFilter.permite(rol,"GET","/reportes/CITAS_OPERATIVAS"+formato));
                assertEquals(rol==Rol.ADMIN,
                        JwtAuthenticationFilter.permite(rol,"GET","/reportes/FINANCIERO"+formato));
            }
            assertFalse(JwtAuthenticationFilter.permite(rol,"POST","/reportes/CITAS_OPERATIVAS"));
        }
    }
}
