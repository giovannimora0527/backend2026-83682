package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/** Contrato REST del catálogo de razas. */
@Tag(name = "Razas", description = "Catálogo de razas y especies")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
@RequestMapping("/raza")
public interface RazaApi {

    /** Datos de una raza: nombre y especie. */
    record RazaRq(String nombre, String especie) {
    }

    /**
     * Listar razas.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar razas")
    @GetMapping("/listar")
    ResponseEntity<List<Map<String, Object>>> listar();

    /**
     * Registrar raza.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar raza")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping("/guardar")
    ResponseEntity<Map<String, Object>> guardar(@RequestBody RazaRq datos);
}
