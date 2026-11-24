package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.models.ReporteCatalogoRs;
import com.uniminuto.clinica.models.ReporteRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@Tag(name = "Reportes")
@RequestMapping("/reporte")
public interface ReporteApi {

    @Operation(summary = "Catálogo de reportes y sus filtros")
    @GetMapping(value = "/catalogo", produces = {"application/json"})
    ResponseEntity<List<ReporteCatalogoRs>> catalogo();

    /** Vista previa. Los filtros de cada reporte estan en el catalogo. */
    @Operation(summary = "Vista previa de un reporte", description = "Los filtros se envían como parámetros; ver el catálogo.")
    @GetMapping(value = "/{codigo}", produces = {"application/json"})
    ResponseEntity<ReporteRs> generar(@PathVariable("codigo") String codigo,
                                      @RequestParam Map<String, String> filtros);

    @Operation(summary = "Exportar un reporte a PDF o Excel")
    @GetMapping(value = "/{codigo}/exportar")
    ResponseEntity<byte[]> exportar(@PathVariable("codigo") String codigo, @RequestParam("formato") String formato,
                                    @RequestParam Map<String, String> filtros);
}
