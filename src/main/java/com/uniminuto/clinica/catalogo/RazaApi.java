package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Map;

/**
 * Contrato REST del catálogo de razas.
 *
 * <p>Reglas: el par nombre + especie es único; no se puede cambiar la especie de una raza que ya
 * tiene mascotas registradas, ni eliminar una raza en uso (se conserva la integridad de las mascotas).</p>
 */
@Tag(name = "Razas", description = "Catálogo de razas y especies")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
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
    @GetMapping("/listar")
    ResponseEntity<List<Map<String, Object>>> listar();

    /**
     * Registrar raza.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con la raza creada.
     */
    @Operation(summary = "Registrar raza")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping("/guardar")
    ResponseEntity<Map<String, Object>> guardar(@RequestBody RazaRq datos);

    /**
     * Actualizar raza (no permite cambiar la especie si ya tiene mascotas).
     *
     * @param id identificador de la raza.
     * @param datos nuevos datos.
     * @return respuesta HTTP con la raza actualizada.
     */
    @Operation(summary = "Actualizar raza")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PutMapping("/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable int id, @RequestBody RazaRq datos);

    /**
     * Eliminar una raza que no tenga mascotas registradas.
     *
     * @param id identificador de la raza.
     * @return respuesta HTTP 204 sin contenido.
     */
    @Operation(summary = "Eliminar raza sin mascotas asociadas")
    @Permitido(Perfiles.ADMINISTRADOR)
    @DeleteMapping("/eliminar/{id}")
    ResponseEntity<Void> eliminar(@PathVariable int id);
}
