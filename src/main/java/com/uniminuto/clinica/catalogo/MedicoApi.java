package com.uniminuto.clinica.catalogo;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST del catálogo de médicos veterinarios.
 *
 * <p>Reglas: documento y registro profesional únicos, especialidad activa, usuario asociado con perfil
 * Médico y sin otro médico, y no se puede inactivar a un médico con citas futuras reservadas.</p>
 */
@Tag(name = "Médicos", description = "Médicos veterinarios")
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
    @PreAuthorize("hasAnyAuthority('MEDICO_CONSULTAR', 'REPORTE_FINANCIERO')")
    @GetMapping("/listar")
    ResponseEntity<List<MedicoDto>> listar();

    /**
     * Listar médicos activos (asignables a citas).
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar médicos activos (asignables a citas)")
    @PreAuthorize("hasAnyAuthority('MEDICO_CONSULTAR', 'REPORTE_FINANCIERO')")
    @GetMapping("/listar-disponibles")
    ResponseEntity<List<MedicoDto>> disponibles();

    /**
     * Registrar médico.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar médico")
    @PreAuthorize("hasAuthority('MEDICO_GESTIONAR')")
    @PostMapping("/crear")
    ResponseEntity<MedicoDto> crear(@RequestBody MedicoRq datos);

    /**
     * Actualizar médico.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Actualizar médico")
    @PreAuthorize("hasAuthority('MEDICO_GESTIONAR')")
    @PutMapping("/actualizar/{id}")
    ResponseEntity<MedicoDto> actualizar(@PathVariable long id, @RequestBody MedicoRq datos);

    /** Nuevo estado del médico. */
    record EstadoRq(Boolean activo) {
    }

    /**
     * Activar o inactivar un médico. No se puede inactivar si tiene citas futuras pendientes,
     * programadas o confirmadas; primero deben reasignarse o cancelarse.
     *
     * @param id identificador del médico.
     * @param datos nuevo estado.
     * @return respuesta HTTP con el médico actualizado.
     */
    @Operation(summary = "Activar o inactivar médico")
    @PreAuthorize("hasAuthority('MEDICO_GESTIONAR')")
    @PatchMapping("/{id}/estado")
    ResponseEntity<MedicoDto> cambiarEstado(@PathVariable long id, @RequestBody EstadoRq datos);
}
