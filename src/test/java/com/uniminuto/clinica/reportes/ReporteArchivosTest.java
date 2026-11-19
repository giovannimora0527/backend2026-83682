package com.uniminuto.clinica.reportes;

import static com.uniminuto.clinica.reportes.ReporteDatos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class ReporteArchivosTest {
    @Test void produceImportesNumericosYFechaDeExcelConLosMismosTotalesDelPdf() throws Exception {
        var generador=new ReporteArchivos();
        var r=resultado(52);
        try(var libro=new XSSFWorkbook(new ByteArrayInputStream(generador.excel(r)))) {
            var detalle=libro.getSheet("Detalle");
            assertEquals(CellType.NUMERIC,detalle.getRow(1).getCell(1).getCellType());
            assertTrue(DateUtil.isCellDateFormatted(detalle.getRow(1).getCell(0)));
            assertEquals(46.32,detalle.getRow(1).getCell(1).getNumericCellValue(),0.00001);
            assertEquals(2408.64,libro.getSheet("Resumen").getRow(3).getCell(1).getNumericCellValue(),0.00001);
            assertEquals(52,detalle.getLastRowNum());
        }
        try(var pdf=Loader.loadPDF(generador.pdf(r))) {
            assertTrue(pdf.getNumberOfPages()>=3);
            String texto=new PDFTextStripper().getText(pdf);
            assertTrue(texto.contains("COP 2408.64"));
            assertTrue(texto.contains("COP 46.32"));
            assertTrue(texto.contains("Página 3"));
        }
    }

    @Test void declaraElResultadoVacio() throws Exception {
        try(var pdf=Loader.loadPDF(new ReporteArchivos().pdf(resultado(0)))) {
            assertTrue(new PDFTextStripper().getText(pdf).contains("No hubo resultados"));
        }
    }

    private Resultado resultado(int n) {
        var precio=new BigDecimal("46.32");
        List<Map<String,Object>> filas=IntStream.range(0,n)
                .mapToObj(i->Map.<String,Object>of("fecha",LocalDate.of(2026,11,19).plusDays(i),"emitido",precio)).toList();
        return new Resultado("Reporte de prueba",LocalDateTime.of(2026,11,19,13,0),Map.of("Zona","America/Bogota"),
                List.of(fecha("fecha","Fecha"),dinero("emitido","Emitido",true)),
                Map.of("emitido",precio.multiply(BigDecimal.valueOf(n))),filas,n,0);
    }
}
