package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Consulta de especialidades para listas desplegables. La administración (registro, edición,
 * activación y tarifas) está en el módulo independiente {@code /tarifario}.
 */
@Tag(name = "Especialidades", description = "Consulta de especialidades médicas")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
@RequestMapping("/especializacion")
public interface EspecializacionApi {

    /**
     * Listar especialidades.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar especialidades")
    @GetMapping("/listar")
    ResponseEntity<List<Map<String, Object>>> listar();
}
