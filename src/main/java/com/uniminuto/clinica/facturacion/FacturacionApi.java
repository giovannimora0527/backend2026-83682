package com.uniminuto.clinica.facturacion;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Requerimiento 3.4 y 3.5: facturación y pagos (ADMINISTRADOR y RECEPCION; anulación y descuentos solo ADMINISTRADOR).
 */
@Tag(name = "Facturación", description = "Facturas de servicios veterinarios, pagos y anulaciones")
@RequestMapping("/facturacion")
public interface FacturacionApi {

    /** Descuento global (solo ADMINISTRADOR) y su motivo para la vista previa de la factura. */
    record PreviaRq(String descuento, String motivo) {
    }

    /** Emisión de factura: clave de idempotencia, huella de la vista previa, descuento y motivo. */
    record EmitirRq(String clave, String previa, String descuento, String motivo) {
    }

    /** Pago de factura: clave de idempotencia, importe, saldo esperado, método y referencia. */
    record PagoRq(String clave, String importe, BigDecimal saldoEsperado, String metodo, String referencia) {
    }

    /** Motivo de la operación (obligatorio para trazabilidad). */
    record MotivoRq(String motivo) {
    }

    /**
     * Atenciones finalizadas pendientes de facturar.
     *
     * @param pagina número de página (desde 0); opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Atenciones finalizadas pendientes de facturar")
    @PreAuthorize("hasAnyAuthority('FACTURA_EMITIR', 'FACTURA_CONSULTAR')")
    @GetMapping("/atenciones")
    ResponseEntity<List<Map<String, Object>>> elegibles(@RequestParam(required = false) Integer pagina);

    /**
     * Vista previa de la factura con huella de importes.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Vista previa de la factura con huella de importes")
    @PreAuthorize("hasAuthority('FACTURA_EMITIR')")
    @PostMapping("/atenciones/{id}/previa")
    ResponseEntity<Map<String, Object>> previa(@PathVariable long id, @RequestBody PreviaRq datos);

    /**
     * Emitir factura (idempotente por clave; exige la huella de la vista previa).
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Emitir factura (idempotente por clave; exige la huella de la vista previa)")
    @PreAuthorize("hasAuthority('FACTURA_EMITIR')")
    @PostMapping("/atenciones/{id}/facturas")
    ResponseEntity<Map<String, Object>> emitir(@PathVariable long id, @RequestBody EmitirRq datos);

    /**
     * Historial de facturación por fechas, propietario, mascota y estado.
     *
     * @param desde fecha inicial del rango (AAAA-MM-DD).
     * @param hasta fecha final del rango (AAAA-MM-DD).
     * @param propietario nombre, documento o identificador del propietario; opcional.
     * @param mascota nombre o identificador de la mascota; opcional.
     * @param estado estado por el que se filtra; opcional.
     * @param pagina número de página (desde 0); opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Historial de facturación por fechas, propietario, mascota y estado")
    @PreAuthorize("hasAuthority('FACTURA_CONSULTAR')")
    @GetMapping("/facturas")
    ResponseEntity<List<Map<String, Object>>> listar(@RequestParam(required = false) String desde,
                                                     @RequestParam(required = false) String hasta,
                                                     @RequestParam(required = false) String propietario,
                                                     @RequestParam(required = false) String mascota,
                                                     @RequestParam(required = false) String estado,
                                                     @RequestParam(required = false) Integer pagina);

    /**
     * Detalle de factura: líneas, pagos e historial de estados.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Detalle de factura: líneas, pagos e historial de estados")
    @PreAuthorize("hasAuthority('FACTURA_CONSULTAR')")
    @GetMapping("/facturas/{id}")
    ResponseEntity<Map<String, Object>> detalle(@PathVariable long id);

    /**
     * Registrar pago (total o parcial).
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar pago (total o parcial)")
    @PreAuthorize("hasAuthority('PAGO_REGISTRAR')")
    @PostMapping("/facturas/{id}/pagos")
    ResponseEntity<Map<String, Object>> pagar(@PathVariable long id, @RequestBody PagoRq datos);

    /**
     * Anular factura (procedimiento controlado; conserva el histórico).
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Anular factura (procedimiento controlado; conserva el histórico)")
    @PreAuthorize("hasAuthority('FACTURA_ANULAR')")
    @PostMapping("/facturas/{id}/anular")
    ResponseEntity<Map<String, Object>> anular(@PathVariable long id, @RequestBody MotivoRq datos);

    /**
     * Factura en PDF.
     *
     * @param id identificador del registro.
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Factura en PDF")
    @PreAuthorize("hasAuthority('FACTURA_CONSULTAR')")
    @GetMapping("/facturas/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable long id);
}
