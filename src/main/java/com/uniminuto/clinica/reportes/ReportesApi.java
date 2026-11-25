package com.uniminuto.clinica.reportes;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Requerimiento 4: reportes con vista previa paginada y exportación a PDF y Excel.
 *
 * <p>Tipos: FINANCIERO, FACTURAS, PAGOS, POR_MEDICO, POR_ESPECIALIDAD, CONCEPTOS, EXISTENCIAS, LOTES,
 * MOVIMIENTOS, DISPENSACIONES, CITAS, CONSULTAS, ATENCIONES, CITAS_OPERATIVAS.</p>
 * <p>Filtros (query string): desde, hasta (AAAA-MM-DD), medico, especialidad, medicamento, lote, mascota,
 * propietario (identificadores), estado y agrupacion (DIA, MES, ANIO para FINANCIERO).</p>
 */
@Tag(name = "Reportes", description = "Reportes financieros, de inventario y de gestión médica (PDF y Excel)")
@RequestMapping("/reportes")
public interface ReportesApi {

    /**
     * Consultar reporte (vista previa paginada, 50 filas por página, con totales).
     *
     * @param tipo tipo de reporte (FINANCIERO, FACTURAS, CITAS, etc.).
     * @param filtros filtros del reporte enviados en la consulta (desde, hasta, medico, estado, etc.).
     * @param pagina número de página (desde 0); opcional.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Consultar reporte (vista previa paginada, 50 filas por página, con totales)")
    @PreAuthorize("hasAnyAuthority('REPORTE_FINANCIERO', 'REPORTE_INVENTARIO', 'REPORTE_CITAS', 'REPORTE_MEDICO', 'REPORTE_DESPACHO')")
    @GetMapping("/{tipo}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable String tipo,
                                                  @Parameter(hidden = true) @RequestParam Map<String, String> filtros,
                                                  @RequestParam(required = false) Integer pagina);

    /**
     * Descargar reporte en PDF.
     *
     * @param tipo tipo de reporte (FINANCIERO, FACTURAS, CITAS, etc.).
     * @param filtros filtros del reporte enviados en la consulta (desde, hasta, medico, estado, etc.).
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Descargar reporte en PDF")
    @PreAuthorize("hasAnyAuthority('REPORTE_FINANCIERO', 'REPORTE_INVENTARIO', 'REPORTE_CITAS', 'REPORTE_MEDICO', 'REPORTE_DESPACHO')")
    @GetMapping("/{tipo}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable String tipo, @Parameter(hidden = true) @RequestParam Map<String, String> filtros);

    /**
     * Descargar reporte en Excel (.xlsx).
     *
     * @param tipo tipo de reporte (FINANCIERO, FACTURAS, CITAS, etc.).
     * @param filtros filtros del reporte enviados en la consulta (desde, hasta, medico, estado, etc.).
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Descargar reporte en Excel (.xlsx)")
    @PreAuthorize("hasAnyAuthority('REPORTE_FINANCIERO', 'REPORTE_INVENTARIO', 'REPORTE_CITAS', 'REPORTE_MEDICO', 'REPORTE_DESPACHO')")
    @GetMapping("/{tipo}/excel")
    ResponseEntity<byte[]> excel(@PathVariable String tipo, @Parameter(hidden = true) @RequestParam Map<String, String> filtros);
}
