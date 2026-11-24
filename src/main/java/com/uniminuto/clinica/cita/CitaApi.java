package com.uniminuto.clinica.cita;

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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Requerimiento 2: gestión de citas médicas mediante calendario.
 * El perfil MEDICO usa la agenda propia (/agenda/mia); ADMINISTRADOR y RECEPCION gestionan todas las citas.
 */
@Tag(name = "Citas y agenda", description = "Citas médicas, calendario del veterinario y disponibilidad")
@RequestMapping("/cita")
public interface CitaApi {

    /** Datos para registrar o editar una cita (fechas en formato AAAA-MM-DDTHH:MM). */
    record CitaRq(Long clienteId, Long mascotaId, Long medicoId, String fechaHora, String fechaFin, String estado,
                  String motivo) {
    }

    /** Cambio de estado u horario de una cita (los campos no enviados no cambian). */
    record CambioRq(String estado, String inicio, String fin) {
    }

    /** Franja semanal de atención: día (1 = lunes ... 7 = domingo) y horas de inicio y fin (HH:MM). */
    record Franja(Integer dia, String inicio, String fin) {
    }

    /** Ausencia del veterinario: inicio, fin y motivo. */
    record AusenciaRq(String inicio, String fin, String motivo) {
    }

    /** Estado activo o inactivo de un médico. */
    record ActividadRq(Boolean activo) {
    }

    // ---------------------------------------------------------------- Citas

    /**
     * Listar citas.
     * <p>El médico solo recibe sus propias citas.</p>
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar citas", description = "El médico solo recibe sus propias citas.")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @GetMapping("/listar")
    ResponseEntity<List<Map<String, Object>>> listar();

    /**
     * Filtrar citas entre una fecha inicial y una final (de la más reciente a la más antigua).
     *
     * @param fechaInicio fecha inicial del rango (AAAA-MM-DD); opcional.
     * @param fechaFin fecha final del rango (AAAA-MM-DD); opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Filtrar citas entre una fecha inicial y una final (de la más reciente a la más antigua)")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @GetMapping("/listar-rango")
    ResponseEntity<List<Map<String, Object>>> listarRango(@RequestParam(required = false) String fechaInicio,
                                                          @RequestParam(required = false) String fechaFin);

    /**
     * Citas de una mascota (para combos dependientes mascota → cita).
     *
     * @param mascotaId identificador de la mascota.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Citas de una mascota (para combos dependientes mascota → cita)")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @GetMapping("/por-mascota/{mascotaId}")
    ResponseEntity<List<Map<String, Object>>> porMascota(@PathVariable int mascotaId);

    /**
     * Consultar una cita.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Consultar una cita")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @GetMapping("/{id:\\d+}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable long id);

    /**
     * Reactivar una cita cancelada (operación explícita; vuelve a pendiente).
     *
     * @param id identificador del registro.
     * @param horario nuevo inicio y fin de la cita; opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Reactivar una cita cancelada (operación explícita; vuelve a pendiente)")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping("/{id}/reactivar")
    ResponseEntity<Map<String, Object>> reactivar(@PathVariable long id, @RequestBody(required = false) CambioRq horario);

    /**
     * Registrar cita.
     * <p>Valida horario, ausencias, médico activo y superposiciones.</p>
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar cita", description = "Valida horario, ausencias, médico activo y superposiciones.")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping("/crear")
    ResponseEntity<Map<String, Object>> crear(@RequestBody CitaRq datos);

    /**
     * Editar cita existente.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Editar cita existente")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PutMapping("/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable long id, @RequestBody CitaRq datos);

    /**
     * Historial de citas de una mascota (paginado de a 10).
     *
     * @param id identificador del registro.
     * @param pagina número de página (desde 0); opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Historial de citas de una mascota (paginado de a 10)")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @GetMapping("/mascotas/{id}/historial")
    ResponseEntity<Map<String, Object>> historialMascota(@PathVariable int id,
                                                        @RequestParam(required = false) Integer pagina);

    // ---------------------------------------------------------------- Agenda del médico autenticado

    /**
     * Calendario del médico autenticado.
     * <p>Rango de fechas (día, semana o mes) y estado opcional.</p>
     *
     * @param desde fecha inicial del rango (AAAA-MM-DD).
     * @param hasta fecha final del rango (AAAA-MM-DD).
     * @param estado estado por el que se filtra; opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Calendario del médico autenticado", description = "Rango de fechas (día, semana o mes) y estado opcional.")
    @Permitido(Perfiles.MEDICO)
    @GetMapping("/agenda/mia")
    ResponseEntity<List<Map<String, Object>>> agendaPropia(@RequestParam String desde, @RequestParam String hasta,
                                                           @RequestParam(required = false) String estado);

    /**
     * Exportar citas del médico autenticado a Excel (.xlsx).
     *
     * @param desde fecha inicial del rango (AAAA-MM-DD).
     * @param hasta fecha final del rango (AAAA-MM-DD).
     * @param estado estado por el que se filtra; opcional.
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Exportar citas del médico autenticado a Excel (.xlsx)")
    @Permitido(Perfiles.MEDICO)
    @GetMapping("/agenda/mia/excel")
    ResponseEntity<byte[]> excelPropio(@RequestParam String desde, @RequestParam String hasta,
                                       @RequestParam(required = false) String estado);

    /**
     * Horario y ausencias del médico autenticado.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Horario y ausencias del médico autenticado")
    @Permitido(Perfiles.MEDICO)
    @GetMapping("/agenda/mia/configuracion")
    ResponseEntity<Map<String, Object>> configuracionPropia();

    /**
     * Guardar horario semanal propio.
     *
     * @param franjas franjas semanales del horario (día, hora de inicio y hora de fin).
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Guardar horario semanal propio")
    @Permitido(Perfiles.MEDICO)
    @PutMapping("/agenda/mia/horarios")
    ResponseEntity<Map<String, Object>> horariosPropios(@RequestBody List<Franja> franjas);

    /**
     * Registrar ausencia propia.
     *
     * @param ausencia periodo y motivo de la ausencia.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar ausencia propia")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/agenda/mia/ausencias")
    ResponseEntity<Map<String, Object>> ausenciaPropia(@RequestBody AusenciaRq ausencia);

    // ---------------------------------------------------------------- Agenda de cualquier médico

    /**
     * Franjas libres de un médico en una fecha.
     *
     * @param medico identificador del médico veterinario.
     * @param fecha fecha a consultar (AAAA-MM-DD).
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Franjas libres de un médico en una fecha")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @GetMapping("/agenda/medicos/{medico}/disponibilidad")
    ResponseEntity<Map<String, Object>> disponibilidad(@PathVariable long medico, @RequestParam String fecha);

    /**
     * Horario y ausencias de un médico.
     *
     * @param medico identificador del médico veterinario.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Horario y ausencias de un médico")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @GetMapping("/agenda/medicos/{medico}/configuracion")
    ResponseEntity<Map<String, Object>> configuracion(@PathVariable long medico);

    /**
     * Guardar horario semanal de un médico.
     *
     * @param medico identificador del médico veterinario.
     * @param franjas franjas semanales del horario (día, hora de inicio y hora de fin).
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Guardar horario semanal de un médico")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PutMapping("/agenda/medicos/{medico}/horarios")
    ResponseEntity<Map<String, Object>> horarios(@PathVariable long medico, @RequestBody List<Franja> franjas);

    /**
     * Registrar ausencia de un médico.
     *
     * @param medico identificador del médico veterinario.
     * @param ausencia periodo y motivo de la ausencia.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar ausencia de un médico")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping("/agenda/medicos/{medico}/ausencias")
    ResponseEntity<Map<String, Object>> ausencia(@PathVariable long medico, @RequestBody AusenciaRq ausencia);

    /**
     * Activar o inactivar un médico.
     *
     * @param medico identificador del médico veterinario.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta sin contenido (204).
     */
    @Operation(summary = "Activar o inactivar un médico")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PutMapping("/agenda/medicos/{medico}/actividad")
    ResponseEntity<Void> actividad(@PathVariable long medico, @RequestBody ActividadRq datos);

    /**
     * Cambiar estado o reprogramar una cita.
     * <p>Queda registrado en el historial.</p>
     *
     * @param id identificador del registro.
     * @param cambio nuevo estado u horario de la cita.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Cambiar estado o reprogramar una cita", description = "Queda registrado en el historial.")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @PutMapping("/agenda/{id}/cambio")
    ResponseEntity<Map<String, Object>> cambiar(@PathVariable long id, @RequestBody CambioRq cambio);

    /**
     * Historial de cambios de una cita.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Historial de cambios de una cita")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
    @GetMapping("/agenda/{id}/historial")
    ResponseEntity<List<Map<String, Object>>> historial(@PathVariable long id);
}
