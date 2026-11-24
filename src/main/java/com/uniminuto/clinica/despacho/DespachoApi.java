package com.uniminuto.clinica.despacho;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
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
@Permitido(Perfiles.DESPACHO_MEDICO)
@RequestMapping("/despacho")
public interface DespachoApi {

    record SeleccionLote(Long medicamento, Long lote, Integer cantidad) {
    }

    record PrepararRq(String clave, List<SeleccionLote> lotes) {
    }

    record MotivoRq(String motivo) {
    }

    @Operation(summary = "Fórmulas confirmadas para despacho",
            description = "Filtros: número, propietario (nombre o documento), mascota, fecha de generación y estado. 50 por página.")
    @GetMapping("/formulas")
    ResponseEntity<List<Map<String, Object>>> formulas(@RequestParam(required = false) String id,
                                                       @RequestParam(required = false) String propietario,
                                                       @RequestParam(required = false) String mascota,
                                                       @RequestParam(required = false) String fecha,
                                                       @RequestParam(required = false) String estado,
                                                       @RequestParam(required = false) Integer pagina);

    @Operation(summary = "Detalle: prescripción, cantidades entregadas y pendientes, lotes vigentes FEFO, "
            + "preparación vigente, entregas e historial de estados")
    @GetMapping("/formulas/{id}")
    ResponseEntity<Map<String, Object>> detalle(@PathVariable long id);

    @Operation(summary = "Preparar despacho: seleccionar lotes y cantidades (no afecta inventario)",
            description = "La clave hace idempotente la solicitud. Se exige prioridad FEFO.")
    @PostMapping("/formulas/{id}/preparar")
    ResponseEntity<Map<String, Object>> preparar(@PathVariable long id, @RequestBody PrepararRq datos);

    @Operation(summary = "Cancelar el despacho de una fórmula (procedimiento autorizado con motivo)")
    @PostMapping("/formulas/{id}/cancelar")
    ResponseEntity<Map<String, Object>> cancelar(@PathVariable long id, @RequestBody MotivoRq datos);

    @Operation(summary = "Marcar la fórmula como pendiente por disponibilidad")
    @PostMapping("/formulas/{id}/disponibilidad")
    ResponseEntity<Map<String, Object>> disponibilidad(@PathVariable long id, @RequestBody MotivoRq datos);

    @Operation(summary = "Historial de despachos (log de despacho de fórmulas)")
    @GetMapping("/entregas")
    ResponseEntity<List<Map<String, Object>>> entregas(@RequestParam(required = false) String desde,
                                                       @RequestParam(required = false) String hasta,
                                                       @RequestParam(required = false) String medicamento,
                                                       @RequestParam(required = false) Integer pagina);

    @Operation(summary = "Confirmar la entrega de una preparación",
            description = "Transacción: descuenta lotes, genera salidas ligadas a la fórmula y al despacho y actualiza el estado.")
    @PostMapping("/entregas/{preparacion}/confirmar")
    ResponseEntity<Map<String, Object>> confirmar(@PathVariable long preparacion);

    @Operation(summary = "Revertir una entrega confirmada (devuelve unidades a sus lotes con movimientos trazables)")
    @PostMapping("/entregas/{id}/revertir")
    ResponseEntity<Map<String, Object>> revertir(@PathVariable long id, @RequestBody MotivoRq datos);

    @Operation(summary = "Comprobante de despacho en PDF")
    @GetMapping("/entregas/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable long id);
}
