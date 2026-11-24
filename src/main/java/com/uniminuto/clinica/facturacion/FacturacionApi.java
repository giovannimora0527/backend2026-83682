package com.uniminuto.clinica.facturacion;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Requerimiento 3.4 y 3.5: facturación y pagos (ADMINISTRADOR y RECEPCION; anulación y descuentos solo ADMINISTRADOR).
 */
@Tag(name = "Facturación", description = "Facturas de servicios veterinarios, pagos y anulaciones")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
@RequestMapping("/facturacion")
public interface FacturacionApi {

    record PreviaRq(String descuento, String motivo) {
    }

    record EmitirRq(String clave, String previa, String descuento, String motivo) {
    }

    record PagoRq(String clave, String importe, BigDecimal saldoEsperado, String metodo, String referencia) {
    }

    record MotivoRq(String motivo) {
    }

    @Operation(summary = "Atenciones finalizadas pendientes de facturar")
    @GetMapping("/atenciones")
    ResponseEntity<List<Map<String, Object>>> elegibles(@RequestParam(required = false) Integer pagina);

    @Operation(summary = "Vista previa de la factura con huella de importes")
    @PostMapping("/atenciones/{id}/previa")
    ResponseEntity<Map<String, Object>> previa(@PathVariable long id, @RequestBody PreviaRq datos);

    @Operation(summary = "Emitir factura (idempotente por clave; exige la huella de la vista previa)")
    @PostMapping("/atenciones/{id}/facturas")
    ResponseEntity<Map<String, Object>> emitir(@PathVariable long id, @RequestBody EmitirRq datos);

    @Operation(summary = "Historial de facturación por fechas, propietario, mascota y estado")
    @GetMapping("/facturas")
    ResponseEntity<List<Map<String, Object>>> listar(@RequestParam(required = false) String desde,
                                                     @RequestParam(required = false) String hasta,
                                                     @RequestParam(required = false) String propietario,
                                                     @RequestParam(required = false) String mascota,
                                                     @RequestParam(required = false) String estado,
                                                     @RequestParam(required = false) Integer pagina);

    @Operation(summary = "Detalle de factura: líneas, pagos e historial de estados")
    @GetMapping("/facturas/{id}")
    ResponseEntity<Map<String, Object>> detalle(@PathVariable long id);

    @Operation(summary = "Registrar pago (total o parcial)")
    @PostMapping("/facturas/{id}/pagos")
    ResponseEntity<Map<String, Object>> pagar(@PathVariable long id, @RequestBody PagoRq datos);

    @Operation(summary = "Anular factura (procedimiento controlado; conserva el histórico)")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/facturas/{id}/anular")
    ResponseEntity<Map<String, Object>> anular(@PathVariable long id, @RequestBody MotivoRq datos);

    @Operation(summary = "Factura en PDF")
    @GetMapping("/facturas/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable long id);
}
