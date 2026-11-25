package com.uniminuto.clinica.catalogo;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST del catálogo de razas.
 *
 * <p>Reglas: el par nombre + especie es único; no se puede cambiar la especie de una raza que ya
 * tiene mascotas registradas, ni eliminar una raza en uso (se conserva la integridad de las mascotas).</p>
 */
@Tag(name = "Razas", description = "Catálogo de razas y especies")
@RequestMapping("/raza")
public interface RazaApi {

    /** Datos de una raza: nombre y especie. */
    record RazaRq(String nombre, String especie) {
    }

    /**
     * Listar razas con el número de mascotas registradas en cada una.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar razas con el número de mascotas de cada una")
    @PreAuthorize("hasAuthority('MASCOTA_CONSULTAR')")
    @GetMapping("/listar")
    ResponseEntity<List<RazaDto>> listar();

    /**
     * Registrar raza.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con la raza creada.
     */
    @Operation(summary = "Registrar raza")
    @PreAuthorize("hasAuthority('RAZA_GESTIONAR')")
    @PostMapping("/guardar")
    ResponseEntity<RazaDto> guardar(@RequestBody RazaRq datos);

    /**
     * Actualizar raza (no permite cambiar la especie si ya tiene mascotas).
     *
     * @param id identificador de la raza.
     * @param datos nuevos datos.
     * @return respuesta HTTP con la raza actualizada.
     */
    @Operation(summary = "Actualizar raza")
    @PreAuthorize("hasAuthority('RAZA_GESTIONAR')")
    @PutMapping("/actualizar/{id}")
    ResponseEntity<RazaDto> actualizar(@PathVariable int id, @RequestBody RazaRq datos);

    /**
     * Eliminar una raza que no tenga mascotas registradas.
     *
     * @param id identificador de la raza.
     * @return respuesta HTTP 204 sin contenido.
     */
    @Operation(summary = "Eliminar raza sin mascotas asociadas")
    @PreAuthorize("hasAuthority('RAZA_ELIMINAR')")
    @DeleteMapping("/eliminar/{id}")
    ResponseEntity<Void> eliminar(@PathVariable int id);
}
