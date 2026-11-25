package com.uniminuto.clinica.despacho;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Requerimiento 5: operado exclusivamente por el perfil DESPACHO_MEDICO (validado en el backend).
 */
@Tag(name = "Despacho", description = "Dispensación de fórmulas médicas con lotes FEFO y trazabilidad")
@RequestMapping("/despacho")
public interface DespachoApi {

    /** Lote y cantidad elegidos para entregar un medicamento de la fórmula. */
    record SeleccionLote(Long medicamento, Long lote, Integer cantidad) {
    }

    /** Preparación de despacho: clave de idempotencia y lotes seleccionados. */
    record PrepararRq(String clave, List<SeleccionLote> lotes) {
    }

    /** Motivo de la operación (obligatorio para trazabilidad). */
    record MotivoRq(String motivo) {
    }

    /**
     * Fórmulas confirmadas para despacho.
     * <p>Filtros: número, propietario (nombre o documento), mascota, fecha de generación y estado. 50 por página.</p>
     *
     * @param id identificador del registro.
     * @param propietario nombre, documento o identificador del propietario; opcional.
     * @param mascota nombre o identificador de la mascota; opcional.
     * @param fecha fecha a consultar (AAAA-MM-DD).
     * @param estado estado por el que se filtra; opcional.
     * @param pagina número de página (desde 0); opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Fórmulas confirmadas para despacho",
            description = "Filtros: número, propietario (nombre o documento), mascota, fecha de generación y estado. 50 por página.")
    @PreAuthorize("hasAuthority('DESPACHO_CONSULTAR')")
    @GetMapping("/formulas")
    ResponseEntity<List<Map<String, Object>>> formulas(@RequestParam(required = false) String id,
                                                       @RequestParam(required = false) String propietario,
                                                       @RequestParam(required = false) String mascota,
                                                       @RequestParam(required = false) String fecha,
                                                       @RequestParam(required = false) String estado,
                                                       @RequestParam(required = false) Integer pagina);

    /**
     * Detalle: prescripción, cantidades entregadas y pendientes, lotes vigentes FEFO, .
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Detalle: prescripción, cantidades entregadas y pendientes, lotes vigentes FEFO, "
            + "preparación vigente, entregas e historial de estados")
    /**
     * Detalle de una fórmula para despacho con líneas, lotes sugeridos y entregas.
     *
     * @param id identificador de la fórmula.
     * @return detalle de la fórmula.
     */
    @PreAuthorize("hasAuthority('DESPACHO_CONSULTAR')")
    @GetMapping("/formulas/{id}")
    ResponseEntity<Map<String, Object>> detalle(@PathVariable long id);

    /**
     * Preparar despacho: seleccionar lotes y cantidades (no afecta inventario).
     * <p>La clave hace idempotente la solicitud. Se exige prioridad FEFO.</p>
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Preparar despacho: seleccionar lotes y cantidades (no afecta inventario)",
            description = "La clave hace idempotente la solicitud. Se exige prioridad FEFO.")
    /**
     * Prepara el despacho seleccionando lotes y cantidades por medicamento.
     *
     * @param id identificador de la fórmula.
     * @param datos selección de lotes.
     * @return plan de preparación.
     */
    @PreAuthorize("hasAuthority('DESPACHO_OPERAR')")
    @PostMapping("/formulas/{id}/preparar")
    ResponseEntity<Map<String, Object>> preparar(@PathVariable long id, @RequestBody PrepararRq datos);

    /**
     * Cancelar el despacho de una fórmula (procedimiento autorizado con motivo).
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Cancelar el despacho de una fórmula (procedimiento autorizado con motivo)")
    @PreAuthorize("hasAuthority('DESPACHO_OPERAR')")
    @PostMapping("/formulas/{id}/cancelar")
    ResponseEntity<Map<String, Object>> cancelar(@PathVariable long id, @RequestBody MotivoRq datos);

    /**
     * Marcar la fórmula como pendiente por disponibilidad.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Marcar la fórmula como pendiente por disponibilidad")
    @PreAuthorize("hasAuthority('DESPACHO_OPERAR')")
    @PostMapping("/formulas/{id}/disponibilidad")
    ResponseEntity<Map<String, Object>> disponibilidad(@PathVariable long id, @RequestBody MotivoRq datos);

    /**
     * Historial de despachos (log de despacho de fórmulas).
     *
     * @param desde fecha inicial del rango (AAAA-MM-DD).
     * @param hasta fecha final del rango (AAAA-MM-DD).
     * @param medicamento identificador del medicamento; opcional.
     * @param pagina número de página (desde 0); opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Historial de despachos (log de despacho de fórmulas)")
    @PreAuthorize("hasAuthority('DESPACHO_CONSULTAR')")
    @GetMapping("/entregas")
    ResponseEntity<List<Map<String, Object>>> entregas(@RequestParam(required = false) String desde,
                                                       @RequestParam(required = false) String hasta,
                                                       @RequestParam(required = false) String medicamento,
                                                       @RequestParam(required = false) Integer pagina);

    /**
     * Confirmar la entrega de una preparación.
     * <p>Transacción: descuenta lotes, genera salidas ligadas a la fórmula y al despacho y actualiza el estado.</p>
     *
     * @param preparacion identificador de la preparación de despacho a confirmar.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Confirmar la entrega de una preparación",
            description = "Transacción: descuenta lotes, genera salidas ligadas a la fórmula y al despacho y actualiza el estado.")
    /**
     * Confirma la entrega preparada y descuenta el inventario.
     *
     * @param preparacion identificador del plan de preparación.
     * @return entrega registrada.
     */
    @PreAuthorize("hasAuthority('DESPACHO_OPERAR')")
    @PostMapping("/entregas/{preparacion}/confirmar")
    ResponseEntity<Map<String, Object>> confirmar(@PathVariable long preparacion);

    /**
     * Revertir una entrega confirmada (devuelve unidades a sus lotes con movimientos trazables).
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Revertir una entrega confirmada (devuelve unidades a sus lotes con movimientos trazables)")
    @PreAuthorize("hasAuthority('DESPACHO_OPERAR')")
    @PostMapping("/entregas/{id}/revertir")
    ResponseEntity<Map<String, Object>> revertir(@PathVariable long id, @RequestBody MotivoRq datos);

    /**
     * Comprobante de despacho en PDF.
     *
     * @param id identificador del registro.
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Comprobante de despacho en PDF")
    @PreAuthorize("hasAuthority('DESPACHO_CONSULTAR')")
    @GetMapping("/entregas/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable long id);
}
