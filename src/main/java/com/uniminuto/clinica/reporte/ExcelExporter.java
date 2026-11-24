package com.uniminuto.clinica.reporte;

import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.ReporteRs;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Exporta cualquier reporte con celdas tipadas. */
@Component
public class ExcelExporter {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    public byte[] exportar(ConfiguracionRs clinica, ReporteRs reporte) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");
            DataFormat formatos = libro.createDataFormat();
            CellStyle titulo = estilo(libro, true, false, null, formatos);
            CellStyle encabezado = estilo(libro, true, true, null, formatos);
            int fila = 0;
            celda(hoja.createRow(fila++), 0, clinica.nombre(), titulo);
            celda(hoja.createRow(fila++), 0, reporte.titulo(), titulo);
            for (Map.Entry<String, Object> filtro : reporte.filtros().entrySet()) {
                Row r = hoja.createRow(fila++);
                celda(r, 0, filtro.getKey(), null);
                celda(r, 1, String.valueOf(filtro.getValue()), null);
            }
            Row generado = hoja.createRow(fila++);
            celda(generado, 0, "Generado", null);
            celda(generado, 1, FECHA_HORA.format(reporte.generadoEn()) + " por " + reporte.generadoPor(), null);
            fila++;
            Row cabecera = hoja.createRow(fila);
            Map<TipoColumna, CellStyle> estilos = new EnumMap<>(TipoColumna.class);
            Map<TipoColumna, CellStyle> estilosTotal = new EnumMap<>(TipoColumna.class);
            for (TipoColumna tipo : TipoColumna.values()) {
                estilos.put(tipo, estilo(libro, false, false, tipo, formatos));
                estilosTotal.put(tipo, estilo(libro, true, true, tipo, formatos));
            }
            List<Columna> columnas = reporte.columnas();
            int[] anchos = new int[columnas.size()];
            for (int i = 0; i < columnas.size(); i++) {
                celda(cabecera, i, columnas.get(i).titulo(), encabezado);
                anchos[i] = columnas.get(i).titulo().length();
            }
            hoja.createFreezePane(0, fila + 1);
            fila++;
            for (Map<String, Object> datos : reporte.filas()) {
                escribir(hoja.createRow(fila++), columnas, datos, estilos, anchos);
            }
            if (!reporte.totales().isEmpty()) {
                Row total = hoja.createRow(fila);
                escribir(total, columnas, reporte.totales(), estilosTotal, anchos);
                if (total.getCell(0).getCellType() == CellType.BLANK) {
                    celda(total, 0, "TOTAL", estilosTotal.get(TipoColumna.TEXTO));
                }
            }
            for (int i = 0; i < anchos.length; i++) {
                hoja.setColumnWidth(i, Math.min(45, Math.max(12, anchos[i] + 3)) * 256);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void escribir(Row fila, List<Columna> columnas, Map<String, Object> datos,
                                 Map<TipoColumna, CellStyle> estilos, int[] anchos) {
        for (int i = 0; i < columnas.size(); i++) {
            Columna columna = columnas.get(i);
            Object valor = datos.get(columna.clave());
            Cell celda = fila.createCell(i);
            celda.setCellStyle(estilos.get(columna.tipo()));
            if (valor != null) {
                switch (valor) {
                    case LocalDateTime f -> celda.setCellValue(f);
                    case LocalDate f -> celda.setCellValue(f);
                    case Number n -> celda.setCellValue(n.doubleValue());
                    default -> celda.setCellValue(valor.toString());
                }
                anchos[i] = Math.max(anchos[i], Math.min(16, valor.toString().length()) == 16
                        ? (columna.tipo() == TipoColumna.TEXTO ? valor.toString().length() : 16)
                        : valor.toString().length() + (columna.tipo() == TipoColumna.MONEDA ? 4 : 0));
            }
        }
    }

    private static void celda(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }

    private static CellStyle estilo(XSSFWorkbook libro, boolean negrita, boolean fondo, TipoColumna tipo,
                                    DataFormat formatos) {
        CellStyle estilo = libro.createCellStyle();
        if (negrita) {
            Font fuente = libro.createFont();
            fuente.setBold(true);
            estilo.setFont(fuente);
        }
        if (fondo) {
            estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (tipo != null) {
            switch (tipo) {
                case MONEDA -> estilo.setDataFormat(formatos.getFormat("\"$\" #,##0.00"));
                case NUMERO -> estilo.setDataFormat(formatos.getFormat("#,##0"));
                case PORCENTAJE -> estilo.setDataFormat(formatos.getFormat("0.##\" %\""));
                case FECHA -> estilo.setDataFormat(formatos.getFormat("dd/mm/yyyy"));
                case FECHA_HORA -> estilo.setDataFormat(formatos.getFormat("dd/mm/yyyy hh:mm"));
                case TEXTO -> { }
            }
        }
        return estilo;
    }
}
