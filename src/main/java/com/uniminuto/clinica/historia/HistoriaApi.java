package com.uniminuto.clinica.historia;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Historias clínicas, anotaciones y fórmulas médicas (primer y segundo parcial).
 * ADMINISTRADOR y MEDICO gestionan; RECEPCION solo consulta fórmulas.
 */
@Tag(name = "Historia clínica", description = "Historias clínicas por consulta, anotaciones y fórmulas médicas")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO})
public interface HistoriaApi {

    /**
     * Datos de una historia clínica.
     *
     * @param clienteId     propietario (debe ser dueño de la mascota).
     * @param mascotaId     mascota.
     * @param citaId        consulta atendida.
     * @param medicoId      veterinario (por defecto, el de la cita).
     * @param fecha         fecha y hora de la consulta (por defecto, la de la cita).
     * @param motivo        motivo de consulta.
     * @param diagnostico   diagnóstico.
     * @param observaciones observaciones.
     * @param activo        estado (solo en actualización).
     */
    record HistoriaRq(Long clienteId, Integer mascotaId, Long citaId, Long medicoId, String fecha, String motivo,
                      String diagnostico, String observaciones, Boolean activo) {
    }

    /**
     * Datos de una anotación.
     *
     * @param historiaId  historia clínica.
     * @param medicoId    veterinario (por defecto, el de la historia).
     * @param fecha       fecha y hora (por defecto, ahora).
     * @param descripcion descripción.
     * @param observacion observación.
     * @param diagnostico diagnóstico o evolución.
     */
    record AnotacionRq(Long historiaId, Long medicoId, String fecha, String descripcion, String observacion,
                       String diagnostico) {
    }

    /**
     * Datos de una fórmula médica.
     *
     * @param clienteId           propietario (opcional; si se envía debe ser el dueño de la mascota).
     * @param mascotaId           mascota (debe coincidir con la de la cita).
     * @param citaId              consulta.
     * @param medicoId            veterinario (por defecto, el de la cita).
     * @param medicamentoId       medicamento.
     * @param dosis               dosis (debe incluir una cantidad mayor que cero).
     * @param frecuencia          frecuencia de administración.
     * @param duracionTratamiento duración (debe incluir un número mayor que cero).
     * @param indicaciones        indicaciones.
     * @param fechaEmision        fecha de emisión (no anterior a la consulta).
     */
    record FormulaRq(Long clienteId, Integer mascotaId, Long citaId, Long medicoId, Long medicamentoId, String dosis,
                     String frecuencia, String duracionTratamiento, String indicaciones, String fechaEmision) {
    }

    /**
     * Cambio de estado de una fórmula.
     *
     * @param activo true para activar, false para inactivar.
     */
    record EstadoRq(Boolean activo) {
    }

    // ------------------------------------------------------------------ Historias clínicas

    /**
     * @param fechaInicio fecha inicial (opcional).
     * @param fechaFin    fecha final (opcional).
     * @param mascotaId   mascota (opcional).
     * @param clienteId   cliente (opcional).
     * @return historias de la más reciente a la más antigua.
     */
    @Operation(summary = "Listar historias clínicas (filtro por rango de fechas, mascota y cliente; más recientes primero)")
    @GetMapping({"/historia-medica/listar-todos", "/historia-medica/listar"})
    ResponseEntity<List<Map<String, Object>>> historias(@RequestParam(required = false) String fechaInicio,
                                                        @RequestParam(required = false) String fechaFin,
                                                        @RequestParam(required = false) Integer mascotaId,
                                                        @RequestParam(required = false) Long clienteId);

    /**
     * @param pacienteId mascota.
     * @return historias de la mascota.
     */
    @Operation(summary = "Historias clínicas de una mascota")
    @GetMapping("/historia-medica/listar-por-paciente/{pacienteId}")
    ResponseEntity<List<Map<String, Object>>> historiasPorPaciente(@PathVariable int pacienteId);

    /**
     * @param id historia.
     * @return detalle con anotaciones y fórmulas.
     */
    @Operation(summary = "Consultar el detalle de una historia clínica con sus anotaciones y fórmulas")
    @GetMapping("/historia-medica/{id:\\d+}")
    ResponseEntity<Map<String, Object>> historia(@PathVariable long id);

    /**
     * @param datos historia a crear.
     * @return historia creada.
     */
    @Operation(summary = "Crear historia clínica para una cita atendida")
    @PostMapping({"/historia-medica/crear", "/historia-medica"})
    ResponseEntity<Map<String, Object>> crearHistoria(@RequestBody HistoriaRq datos);

    /**
     * @param id    historia.
     * @param datos nuevos datos.
     * @return historia actualizada.
     */
    @Operation(summary = "Actualizar historia clínica (no cambia mascota ni cita)")
    @PutMapping("/historia-medica/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizarHistoria(@PathVariable long id, @RequestBody HistoriaRq datos);

    /**
     * @param id historia.
     * @return anotaciones de la historia.
     */
    @Operation(summary = "Consultar las anotaciones de una historia clínica (más recientes primero)")
    @GetMapping("/historia-medica/{id}/anotaciones")
    ResponseEntity<List<Map<String, Object>>> anotacionesDeHistoria(@PathVariable long id);

    // ------------------------------------------------------------------ Anotaciones

    /**
     * @param fechaInicio fecha inicial (opcional).
     * @param fechaFin    fecha final (opcional).
     * @param historiaId  historia (opcional).
     * @return anotaciones.
     */
    @Operation(summary = "Listar anotaciones (filtro por fechas e historia; más recientes primero)")
    @GetMapping({"/anotacion-historia/listar-todos", "/anotacion-historia/listar"})
    ResponseEntity<List<Map<String, Object>>> anotaciones(@RequestParam(required = false) String fechaInicio,
                                                          @RequestParam(required = false) String fechaFin,
                                                          @RequestParam(required = false) Long historiaId);

    /**
     * @param id anotación.
     * @return anotación.
     */
    @Operation(summary = "Consultar una anotación")
    @GetMapping("/anotacion-historia/{id:\\d+}")
    ResponseEntity<Map<String, Object>> anotacion(@PathVariable long id);

    /**
     * @param datos anotación a crear.
     * @return anotación creada.
     */
    @Operation(summary = "Crear anotación en una historia clínica")
    @PostMapping({"/anotacion-historia/crear", "/anotacion-historia"})
    ResponseEntity<Map<String, Object>> crearAnotacion(@RequestBody AnotacionRq datos);

    /**
     * @param id    anotación.
     * @param datos nuevos datos.
     * @return anotación actualizada.
     */
    @Operation(summary = "Actualizar una anotación")
    @PutMapping("/anotacion-historia/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizarAnotacion(@PathVariable long id, @RequestBody AnotacionRq datos);

    // ------------------------------------------------------------------ Fórmulas médicas

    /**
     * @return fórmulas ordenadas por fecha de creación descendente.
     */
    @Operation(summary = "Listar fórmulas médicas ordenadas por fecha de creación (más recientes primero)")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO, Perfiles.RECEPCION})
    @GetMapping({"/formula-medica/listar-todos", "/formula-medica/listar"})
    ResponseEntity<List<Map<String, Object>>> formulas();

    /**
     * @param id fórmula.
     * @return detalle completo.
     */
    @Operation(summary = "Consultar una fórmula médica")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO, Perfiles.RECEPCION})
    @GetMapping("/formula-medica/consultar/{id}")
    ResponseEntity<Map<String, Object>> formula(@PathVariable long id);

    /**
     * @param datos fórmula a crear.
     * @return fórmula creada.
     */
    @Operation(summary = "Crear fórmula médica (valida mascota, cliente, cita, veterinario y fechas)")
    @PostMapping("/formula-medica/crear")
    ResponseEntity<Map<String, Object>> crearFormula(@RequestBody FormulaRq datos);

    /**
     * @param id    fórmula.
     * @param datos nuevos datos.
     * @return fórmula actualizada.
     */
    @Operation(summary = "Editar fórmula médica activa (no cambia cita ni mascota)")
    @PutMapping("/formula-medica/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizarFormula(@PathVariable long id, @RequestBody FormulaRq datos);

    /**
     * @param id    fórmula.
     * @param datos nuevo estado.
     * @return fórmula actualizada.
     */
    @Operation(summary = "Activar o inactivar una fórmula (no hay eliminación física)")
    @PatchMapping("/formula-medica/{id}/estado")
    ResponseEntity<Map<String, Object>> estadoFormula(@PathVariable long id, @RequestBody EstadoRq datos);
}
