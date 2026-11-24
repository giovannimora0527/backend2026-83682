package com.uniminuto.clinica.comun;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generador de libros Excel (.xlsx) con encabezado, fecha de generación, filtros,
 * tabla de datos y fila de totales.
 */
public final class ExcelLibro {

    /** Tipos de formato soportados para las columnas. */
    public enum Formato { TEXTO, ENTERO, COP, FECHA, FECHAHORA }

    /** Definición de una columna: clave en la fila, título visible y formato. */
    public record Columna(String clave, String titulo, Formato formato) {
    }

    private ExcelLibro() {
    }

    public static byte[] generar(String titulo, Map<String, String> filtros, List<Columna> columnas,
                                 List<Map<String, Object>> filas, Map<String, Object> totales) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");
            CellStyle estiloTitulo = libro.createCellStyle();
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            estiloTitulo.setFont(fuenteTitulo);

            CellStyle cabecera = libro.createCellStyle();
            Font fuenteCabecera = libro.createFont();
            fuenteCabecera.setBold(true);
            fuenteCabecera.setColor(IndexedColors.WHITE.getIndex());
            cabecera.setFont(fuenteCabecera);
            cabecera.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecera.setBorderBottom(BorderStyle.THIN);

            CellStyle moneda = libro.createCellStyle();
            moneda.setDataFormat(libro.createDataFormat().getFormat("\"$\" #,##0.00"));
            CellStyle entero = libro.createCellStyle();
            entero.setDataFormat(libro.createDataFormat().getFormat("#,##0"));
            CellStyle negrita = libro.createCellStyle();
            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            negrita.setFont(fuenteNegrita);
            CellStyle monedaTotal = libro.createCellStyle();
            monedaTotal.cloneStyleFrom(moneda);
            monedaTotal.setFont(fuenteNegrita);

            int r = 0;
            celda(hoja.createRow(r++), 0, PdfDocumento.CLINICA + " · " + PdfDocumento.NIT, estiloTitulo);
            celda(hoja.createRow(r++), 0, titulo, estiloTitulo);
            celda(hoja.createRow(r++), 0, "Generado: "
                    + Tiempo.ahora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), null);
            if (filtros != null) {
                for (Map.Entry<String, String> f : filtros.entrySet()) {
                    Row fila = hoja.createRow(r++);
                    celda(fila, 0, f.getKey(), negrita);
                    celda(fila, 1, f.getValue(), null);
                }
            }
            r++;
            Row filaCabecera = hoja.createRow(r++);
            for (int i = 0; i < columnas.size(); i++) {
                celda(filaCabecera, i, columnas.get(i).titulo(), cabecera);
            }
            for (Map<String, Object> dato : filas) {
                Row fila = hoja.createRow(r++);
                for (int i = 0; i < columnas.size(); i++) {
                    escribir(fila, i, dato.get(columnas.get(i).clave()), columnas.get(i).formato(), moneda, entero);
                }
            }
            if (totales != null && !totales.isEmpty()) {
                Row fila = hoja.createRow(r);
                celda(fila, 0, "TOTALES", negrita);
                for (int i = 0; i < columnas.size(); i++) {
                    Columna c = columnas.get(i);
                    if (totales.containsKey(c.clave())) {
                        escribir(fila, i, totales.get(c.clave()), c.formato(), monedaTotal, negrita);
                    }
                }
            }
            // Ancho fijo calculado por el título: evita depender de fuentes del sistema en contenedores.
            for (int i = 0; i < columnas.size(); i++) {
                int ancho = Math.max(14, Math.min(45, columnas.get(i).titulo().length() + 6));
                hoja.setColumnWidth(i, ancho * 256);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible generar el archivo Excel", e);
        }
    }

    private static void escribir(Row fila, int i, Object valor, Formato formato, CellStyle moneda, CellStyle entero) {
        Cell celda = fila.createCell(i);
        if (valor == null) {
            celda.setCellValue("");
            return;
        }
        if ((formato == Formato.COP || formato == Formato.ENTERO) && valor instanceof Number n) {
            celda.setCellValue(n instanceof BigDecimal b ? b.doubleValue() : n.doubleValue());
            celda.setCellStyle(formato == Formato.COP ? moneda : entero);
            return;
        }
        String texto = valor.toString();
        if (formato == Formato.FECHAHORA) {
            texto = texto.replace('T', ' ');
        }
        celda.setCellValue(texto);
    }

    private static void celda(Row fila, int i, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(i);
        celda.setCellValue(valor);
        if (estilo != null) {
            celda.setCellStyle(estilo);
        }
    }
}
