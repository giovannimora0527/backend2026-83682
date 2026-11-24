package com.uniminuto.clinica.carga;

import com.uniminuto.clinica.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Lee .xlsx y .csv (UTF-8, separado por coma o punto y coma) y valida los encabezados. */
@Component
public class LectorArchivo {

    private final int maxFilas;

    public LectorArchivo(@Value("${inventario.carga.max-filas:5000}") int maxFilas) {
        this.maxFilas = maxFilas;
    }

    /** @return xlsx o csv. */
    public String formato(String nombreArchivo) {
        String nombre = nombreArchivo == null ? "" : nombreArchivo.toLowerCase(Locale.ROOT);
        if (nombre.endsWith(".xlsx")) {
            return "xlsx";
        }
        if (nombre.endsWith(".csv")) {
            return "csv";
        }
        throw new BadRequestException("El archivo debe ser .xlsx o .csv.");
    }

    public List<FilaArchivo> leer(String formato, byte[] contenido) {
        if (contenido.length == 0) {
            throw new BadRequestException("El archivo está vacío.");
        }
        boolean esZip = contenido.length > 3 && contenido[0] == 'P' && contenido[1] == 'K';
        if ("xlsx".equals(formato) != esZip) {
            throw new BadRequestException("El contenido no corresponde a un archivo ." + formato + ".");
        }
        List<FilaArchivo> filas = esZip ? leerExcel(contenido) : leerCsv(contenido);
        if (filas.isEmpty()) {
            throw new BadRequestException("El archivo no tiene filas de datos.");
        }
        return filas;
    }

    private List<FilaArchivo> leerExcel(byte[] contenido) {
        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            Sheet hoja = libro.getSheetAt(0);
            Row encabezado = hoja.getRow(0);
            validarEncabezados(encabezado == null ? List.of() : valores(encabezado, encabezado.getLastCellNum()));
            List<FilaArchivo> filas = new ArrayList<>();
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila != null) {
                    agregar(filas, i + 1, valores(fila, ColumnasCarga.TODAS.size()));
                }
            }
            return filas;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException | RuntimeException ex) {
            throw new BadRequestException("No fue posible leer el archivo Excel.");
        }
    }

    private List<String> valores(Row fila, int columnas) {
        List<String> valores = new ArrayList<>();
        for (int i = 0; i < columnas; i++) {
            valores.add(texto(fila.getCell(i)));
        }
        return valores;
    }

    private String texto(Cell celda) {
        if (celda == null) {
            return "";
        }
        CellType tipo = celda.getCellType() == CellType.FORMULA ? celda.getCachedFormulaResultType()
                : celda.getCellType();
        return switch (tipo) {
            case STRING -> celda.getStringCellValue().trim();
            case BOOLEAN -> celda.getBooleanCellValue() ? "SI" : "NO";
            case NUMERIC -> DateUtil.isCellDateFormatted(celda)
                    ? celda.getLocalDateTimeCellValue().toLocalDate().toString()
                    : BigDecimal.valueOf(celda.getNumericCellValue()).stripTrailingZeros().toPlainString();
            default -> "";
        };
    }

    private List<FilaArchivo> leerCsv(byte[] contenido) {
        String texto = decodificar(contenido);
        int fin = texto.indexOf('\n');
        String primera = fin < 0 ? texto : texto.substring(0, fin);
        char separador = primera.chars().filter(c -> c == ';').count() > primera.chars().filter(c -> c == ',').count()
                ? ';' : ',';
        CSVFormat formato = CSVFormat.DEFAULT.builder().setDelimiter(separador).setTrim(true)
                .setIgnoreEmptyLines(false).get();
        try (CSVParser parser = CSVParser.parse(new StringReader(texto), formato)) {
            List<FilaArchivo> filas = new ArrayList<>();
            for (CSVRecord registro : parser) {
                if (registro.getRecordNumber() == 1) {
                    validarEncabezados(registro.toList());
                } else {
                    if (registro.size() > ColumnasCarga.TODAS.size()) {
                        throw new BadRequestException("La fila " + registro.getRecordNumber()
                                + " tiene más columnas que el encabezado.");
                    }
                    agregar(filas, Math.toIntExact(registro.getRecordNumber()), registro.toList());
                }
            }
            return filas;
        } catch (IOException | UncheckedIOException ex) {
            throw new BadRequestException("No fue posible leer el archivo CSV.");
        }
    }

    private String decodificar(byte[] contenido) {
        try {
            String texto = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(contenido)).toString();
            return texto.startsWith("\uFEFF") ? texto.substring(1) : texto;
        } catch (CharacterCodingException ex) {
            throw new BadRequestException("El archivo CSV debe estar en UTF-8.");
        }
    }

    private void agregar(List<FilaArchivo> filas, int numero, List<String> valores) {
        if (valores.stream().allMatch(String::isBlank)) {
            return;
        }
        if (filas.size() >= maxFilas) {
            throw new BadRequestException("El archivo supera el máximo de " + maxFilas + " filas.");
        }
        Map<String, String> datos = new LinkedHashMap<>();
        for (int i = 0; i < ColumnasCarga.TODAS.size(); i++) {
            datos.put(ColumnasCarga.TODAS.get(i), i < valores.size() ? valores.get(i).trim() : "");
        }
        filas.add(new FilaArchivo(numero, datos));
    }

    private void validarEncabezados(List<String> encabezados) {
        List<String> recibidos = new ArrayList<>(encabezados.stream()
                .map(e -> e.trim().toLowerCase(Locale.ROOT)).toList());
        while (!recibidos.isEmpty() && recibidos.getLast().isEmpty()) {
            recibidos.removeLast();
        }
        if (recibidos.equals(ColumnasCarga.TODAS)) {
            return;
        }
        List<String> faltantes = ColumnasCarga.TODAS.stream().filter(c -> !recibidos.contains(c)).toList();
        List<String> sobrantes = new ArrayList<>(recibidos);
        ColumnasCarga.TODAS.forEach(sobrantes::remove);
        if (faltantes.isEmpty() && sobrantes.isEmpty()) {
            throw new BadRequestException("Los encabezados no están en el orden de la plantilla.");
        }
        throw new BadRequestException("Encabezados incorrectos. Faltan: " + lista(faltantes) + ". Sobran: "
                + lista(sobrantes) + ".");
    }

    private static String lista(List<String> valores) {
        return valores.isEmpty() ? "ninguno" : String.join(", ", valores);
    }
}
