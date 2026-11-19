package com.uniminuto.clinica.reportes;

import static com.uniminuto.clinica.reportes.ReporteDatos.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reportes")
class ReporteControlador {
    private final ReporteServicio servicio;
    private final ReporteArchivos archivos;

    ReporteControlador(ReporteServicio servicio,ReporteArchivos archivos) {
        this.servicio=servicio;
        this.archivos=archivos;
    }

    @GetMapping("/{tipo}")
    Resultado previa(@PathVariable Tipo tipo,@ModelAttribute Filtro filtro,@RequestParam(defaultValue="0") int pagina) {
        var r=servicio.consultar(tipo,filtro,pagina,false);
        return new Resultado(r.titulo(),r.generado(),r.filtros(),r.columnas(),decimales(r.totales()),
                r.filas().stream().map(ReporteControlador::decimales).toList(),r.registros(),r.pagina());
    }

    @GetMapping("/{tipo}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable Tipo tipo,@ModelAttribute Filtro filtro) throws IOException {
        return descarga(tipo,"pdf",MediaType.APPLICATION_PDF,archivos.pdf(servicio.consultar(tipo,filtro,0,true)));
    }

    @GetMapping("/{tipo}/excel")
    ResponseEntity<byte[]> excel(@PathVariable Tipo tipo,@ModelAttribute Filtro filtro) throws IOException {
        return descarga(tipo,"xlsx",MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                archivos.excel(servicio.consultar(tipo,filtro,0,true)));
    }

    private static ResponseEntity<byte[]> descarga(Tipo tipo,String extension,MediaType media,byte[] datos) {
        String nombre="reporte-"+tipo.name().toLowerCase(java.util.Locale.ROOT)+"."+extension;
        return ResponseEntity.ok().contentType(media)
                .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(nombre).build().toString())
                .body(datos);
    }

    private static Map<String,Object> decimales(Map<String,Object> fila) {
        Map<String,Object> salida=new LinkedHashMap<>();
        fila.forEach((k,v)->salida.put(k,v instanceof BigDecimal n?n.toPlainString():v));
        return salida;
    }
}
