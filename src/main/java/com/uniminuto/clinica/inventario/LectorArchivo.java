package com.uniminuto.clinica.inventario;

import com.uniminuto.clinica.exception.BadRequestException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lee archivos CSV (separador coma o punto y coma, con comillas) o Excel (.xlsx) y devuelve cada fila
 * como un mapa {encabezado → valor de texto}. La primera fila debe contener los encabezados.
 */
final class LectorArchivo {

    static final int MAXIMO_FILAS = 5000;

    /** Fila leída con su número en el archivo (incluye la fila de encabezados como 1). */
    record Fila(int numero, Map<String, String> valores) {
    }

    private LectorArchivo() {
    }

    static List<Fila> leer(String nombre, byte[] contenido, List<String> encabezadosEsperados) {
        String n = nombre == null ? "" : nombre.toLowerCase(Locale.ROOT);
        List<List<String>> filas;
        if (n.endsWith(".csv")) {
            filas = csv(new String(contenido, StandardCharsets.UTF_8));
        } else if (n.endsWith(".xlsx")) {
            filas = xlsx(contenido);
        } else {
            throw new BadRequestException("Formato no soportado. Usa un archivo .csv o .xlsx.");
        }
        if (filas.isEmpty()) {
            throw new BadRequestException("El archivo está vacío.");
        }
        List<String> encabezados = filas.get(0).stream().map(h -> h.replace("﻿", "").trim()).toList();
        List<String> faltantes = new ArrayList<>();
        for (String esperado : encabezadosEsperados) {
            if (encabezados.stream().noneMatch(h -> h.equalsIgnoreCase(esperado))) {
                faltantes.add(esperado);
            }
        }
        List<String> desconocidos = encabezados.stream().filter(h -> !h.isEmpty())
                .filter(h -> encabezadosEsperados.stream().noneMatch(e -> e.equalsIgnoreCase(h))).toList();
        if (!faltantes.isEmpty() || !desconocidos.isEmpty()) {
            throw new BadRequestException("Estructura de archivo inválida."
                    + (faltantes.isEmpty() ? "" : " Faltan columnas: " + String.join(", ", faltantes) + ".")
                    + (desconocidos.isEmpty() ? "" : " Columnas no reconocidas: " + String.join(", ", desconocidos) + "."));
        }
        List<Fila> resultado = new ArrayList<>();
        for (int i = 1; i < filas.size(); i++) {
            List<String> valores = filas.get(i);
            if (valores.stream().allMatch(v -> v == null || v.isBlank())) {
                continue;
            }
            Map<String, String> mapa = new LinkedHashMap<>();
            for (int c = 0; c < encabezados.size(); c++) {
                String encabezado = encabezados.get(c);
                String clave = encabezadosEsperados.stream().filter(e -> e.equalsIgnoreCase(encabezado))
                        .findFirst().orElse(null);
                if (clave != null) {
                    mapa.put(clave, c < valores.size() && valores.get(c) != null ? valores.get(c).trim() : "");
                }
            }
            resultado.add(new Fila(i + 1, mapa));
        }
        if (resultado.isEmpty()) {
            throw new BadRequestException("El archivo no tiene registros después de los encabezados.");
        }
        if (resultado.size() > MAXIMO_FILAS) {
            throw new BadRequestException("El archivo supera el máximo de " + MAXIMO_FILAS + " registros.");
        }
        return resultado;
    }

    /** Analizador CSV compatible con RFC 4180 (comillas dobles escapadas como ""). */
    static List<List<String>> csv(String texto) {
        String primera = texto.lines().findFirst().orElse("");
        char separador = primera.chars().filter(ch -> ch == ';').count() > primera.chars().filter(ch -> ch == ',').count()
                ? ';' : ',';
        List<List<String>> filas = new ArrayList<>();
        List<String> actual = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean comillas = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (comillas) {
                if (c == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        campo.append('"');
                        i++;
                    } else {
                        comillas = false;
                    }
                } else {
                    campo.append(c);
                }
            } else if (c == '"') {
                comillas = true;
            } else if (c == separador) {
                actual.add(campo.toString());
                campo.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') {
                    i++;
                }
                actual.add(campo.toString());
                campo.setLength(0);
                filas.add(actual);
                actual = new ArrayList<>();
            } else {
                campo.append(c);
            }
        }
        if (campo.length() > 0 || !actual.isEmpty()) {
            actual.add(campo.toString());
            filas.add(actual);
        }
        return filas;
    }

    static List<List<String>> xlsx(byte[] contenido) {
        try (Workbook libro = WorkbookFactory.create(new ByteArrayInputStream(contenido))) {
            Sheet hoja = libro.getSheetAt(0);
            DataFormatter formato = new DataFormatter(Locale.ROOT);
            List<List<String>> filas = new ArrayList<>();
            int ancho = hoja.getRow(hoja.getFirstRowNum()) == null ? 0 : hoja.getRow(hoja.getFirstRowNum()).getLastCellNum();
            for (int r = hoja.getFirstRowNum(); r <= hoja.getLastRowNum(); r++) {
                Row fila = hoja.getRow(r);
                List<String> valores = new ArrayList<>();
                for (int c = 0; c < ancho; c++) {
                    Cell celda = fila == null ? null : fila.getCell(c);
                    valores.add(texto(celda, formato));
                }
                filas.add(valores);
            }
            return filas;
        } catch (IOException | RuntimeException e) {
            if (e instanceof BadRequestException b) {
                throw b;
            }
            throw new BadRequestException("No fue posible leer el archivo Excel. Verifica que sea un .xlsx válido.");
        }
    }

    private static String texto(Cell celda, DataFormatter formato) {
        if (celda == null) {
            return "";
        }
        CellType tipo = celda.getCellType() == CellType.FORMULA ? celda.getCachedFormulaResultType() : celda.getCellType();
        if (tipo == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(celda)) {
                return celda.getLocalDateTimeCellValue().toLocalDate().toString();
            }
            return new BigDecimal(String.valueOf(celda.getNumericCellValue())).stripTrailingZeros().toPlainString();
        }
        if (tipo == CellType.BOOLEAN) {
            return String.valueOf(celda.getBooleanCellValue());
        }
        return formato.formatCellValue(celda);
    }
}
