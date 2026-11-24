package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/** Contrato REST del catálogo de médicos veterinarios. */
@Tag(name = "Médicos", description = "Médicos veterinarios")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
@RequestMapping("/medico")
public interface MedicoApi {

    /** Datos de un médico veterinario, su especialidad y el usuario del sistema asociado (opcional). */
    record MedicoRq(String tipoDocumento, String numeroDocumento, String nombres, String apellidos, String telefono,
                    String registroProfesional, Long especializacionId, Long usuarioId, Boolean activo) {
    }

    /**
     * Listar médicos con su especialidad.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar médicos con su especialidad")
    @GetMapping("/listar")
    ResponseEntity<List<Map<String, Object>>> listar();

    /**
     * Listar médicos activos (asignables a citas).
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar médicos activos (asignables a citas)")
    @GetMapping("/listar-disponibles")
    ResponseEntity<List<Map<String, Object>>> disponibles();

    /**
     * Registrar médico.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar médico")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/crear")
    ResponseEntity<Map<String, Object>> crear(@RequestBody MedicoRq datos);

    /**
     * Actualizar médico.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Actualizar médico")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PutMapping("/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable long id, @RequestBody MedicoRq datos);
}
