package com.uniminuto.clinica.clinica;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Requerimiento 3.1: módulo administrativo independiente de especialidades y tarifas.
 * Permite cambiar valores de consultas sin modificar el código fuente.
 */
@Tag(name = "Tarifario", description = "Especialidades, servicios, tarifas, IVA e impuestos (con historial)")
@RequestMapping("/tarifario")
public interface TarifarioApi {

    /** Datos de una especialidad: código, nombre, descripción y estado activo. */
    record EspecialidadRq(String codigo, String nombre, String descripcion, Boolean activo) {
    }

    /** Nombre de un servicio o procedimiento de la especialidad. */
    record ServicioRq(String nombre) {
    }

    /** Nueva vigencia de tarifa: valor, IVA y otros impuestos (porcentajes) y fechas de vigencia. */
    record TarifaRq(BigDecimal valor, BigDecimal iva, BigDecimal impuesto, String desde, String hasta) {
    }

    /**
     * Listar especialidades.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar especialidades")
    @PreAuthorize("hasAuthority('TARIFA_CONSULTAR')")
    @GetMapping("/especialidades")
    ResponseEntity<List<Map<String, Object>>> especialidades();

    /**
     * Registrar especialidad.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar especialidad")
    @PreAuthorize("hasAuthority('TARIFA_GESTIONAR')")
    @PostMapping("/especialidades")
    ResponseEntity<Map<String, Object>> crearEspecialidad(@RequestBody EspecialidadRq datos);

    /**
     * Editar, activar o inactivar especialidad.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Editar, activar o inactivar especialidad")
    @PreAuthorize("hasAuthority('TARIFA_GESTIONAR')")
    @PutMapping("/especialidades/{id}")
    ResponseEntity<Map<String, Object>> actualizarEspecialidad(@PathVariable long id, @RequestBody EspecialidadRq datos);

    /**
     * Servicios de una especialidad.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Servicios de una especialidad")
    @PreAuthorize("hasAuthority('TARIFA_CONSULTAR')")
    @GetMapping("/especialidades/{id}/servicios")
    ResponseEntity<List<Map<String, Object>>> servicios(@PathVariable long id);

    /**
     * Agregar servicio o procedimiento a una especialidad.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Agregar servicio o procedimiento a una especialidad")
    @PreAuthorize("hasAuthority('TARIFA_GESTIONAR')")
    @PostMapping("/especialidades/{id}/servicios")
    ResponseEntity<Map<String, Object>> agregarServicio(@PathVariable long id, @RequestBody ServicioRq datos);

    /**
     * Historial de tarifas de un servicio (log de tarifas).
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Historial de tarifas de un servicio (log de tarifas)")
    @PreAuthorize("hasAuthority('TARIFA_CONSULTAR')")
    @GetMapping("/servicios/{id}/tarifas")
    ResponseEntity<List<Map<String, Object>>> tarifas(@PathVariable long id);

    /**
     * Registrar nueva vigencia de tarifa.
     * <p>Cierra la vigencia anterior; no la borra.</p>
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar nueva vigencia de tarifa", description = "Cierra la vigencia anterior; no la borra.")
    @PreAuthorize("hasAuthority('TARIFA_GESTIONAR')")
    @PostMapping("/servicios/{id}/tarifas")
    ResponseEntity<Map<String, Object>> nuevaTarifa(@PathVariable long id, @RequestBody TarifaRq datos);
}
