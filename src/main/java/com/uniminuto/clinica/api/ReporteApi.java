package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.ReporteCatalogoRs;
import com.uniminuto.clinica.models.ReporteRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@RequestMapping("/reporte")
public interface ReporteApi {

    @GetMapping(value = "/catalogo", produces = {"application/json"})
    ResponseEntity<List<ReporteCatalogoRs>> catalogo();

    /** Vista previa. Los filtros de cada reporte estan en el catalogo. */
    @GetMapping(value = "/{codigo}", produces = {"application/json"})
    ResponseEntity<ReporteRs> generar(@PathVariable("codigo") String codigo,
                                      @RequestParam Map<String, String> filtros);

    @GetMapping(value = "/{codigo}/exportar")
    ResponseEntity<byte[]> exportar(@PathVariable("codigo") String codigo, @RequestParam("formato") String formato,
                                    @RequestParam Map<String, String> filtros);
}
