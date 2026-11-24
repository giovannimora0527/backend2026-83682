package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ConfiguracionApi;
import com.uniminuto.clinica.models.ConfiguracionRq;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.ConfiguracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfiguracionApiController implements ConfiguracionApi {

    private final ConfiguracionService servicio;

    public ConfiguracionApiController(ConfiguracionService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConfiguracionRs> obtener() {
        return ResponseEntity.ok(servicio.obtener());
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<ConfiguracionRs>> actualizar(ConfiguracionRq rq) {
        return ResponseEntity.ok(RespuestaRs.ok("Configuración actualizada correctamente.", servicio.actualizar(rq)));
    }
}
