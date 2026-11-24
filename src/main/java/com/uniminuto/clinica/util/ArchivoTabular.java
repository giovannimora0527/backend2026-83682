package com.uniminuto.clinica.util;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Descarga;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Genera tablas sencillas en Excel o CSV. */
public final class ArchivoTabular {

    public static final String TIPO_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String TIPO_CSV = "text/csv; charset=UTF-8";

    private ArchivoTabular() {
    }

    public static Descarga generar(String nombre, String formato, List<String> encabezados,
                                   List<List<String>> filas) {
        return switch (formato == null ? "" : formato.toLowerCase(Locale.ROOT)) {
            case "xlsx" -> new Descarga(nombre + ".xlsx", TIPO_XLSX, excel(encabezados, filas));
            case "csv" -> new Descarga(nombre + ".csv", TIPO_CSV, csv(encabezados, filas));
            default -> throw new BadRequestException("El formato debe ser xlsx o csv.");
        };
    }

    private static byte[] excel(List<String> encabezados, List<List<String>> filas) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Datos");
            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estilo = libro.createCellStyle();
            estilo.setFont(negrita);
            estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            escribir(hoja.createRow(0), encabezados, estilo);
            for (int i = 0; i < filas.size(); i++) {
                escribir(hoja.createRow(i + 1), filas.get(i), null);
            }
            for (int i = 0; i < encabezados.size(); i++) {
                hoja.setColumnWidth(i, 20 * 256);
            }
            hoja.createFreezePane(0, 1);
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void escribir(Row fila, List<String> valores, CellStyle estilo) {
        for (int i = 0; i < valores.size(); i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(valores.get(i) == null ? "" : valores.get(i));
            if (estilo != null) {
                celda.setCellStyle(estilo);
            }
        }
    }

    private static byte[] csv(List<String> encabezados, List<List<String>> filas) {
        try (StringWriter texto = new StringWriter(); CSVPrinter csv = new CSVPrinter(texto, CSVFormat.DEFAULT)) {
            csv.printRecord(encabezados);
            for (List<String> fila : filas) {
                csv.printRecord(fila.stream().map(ArchivoTabular::sinFormula).toList());
            }
            csv.flush();
            return ("\uFEFF" + texto).getBytes(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /** Evita que Excel ejecute como formula un texto que vino de un archivo. */
    private static String sinFormula(String valor) {
        if (valor == null) {
            return "";
        }
        boolean numero = valor.matches("-?\\d+([.,]\\d+)?");
        return !numero && !valor.isEmpty() && "=+-@\t\r".indexOf(valor.charAt(0)) >= 0 ? "'" + valor : valor;
    }
}
