package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.util.ArchivoTabular;
import com.uniminuto.clinica.util.RangoConsulta;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Reporte Excel de citas: encabezado, citas coloreadas por estado y resumen. */
@Component
public class ReporteCitasExcel {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final List<String> COLUMNAS = List.of("Fecha", "Hora inicio", "Hora fin", "Mascota", "Especie",
            "Propietario", "Teléfono", "Motivo", "Servicio", "Estado");
    private static final int[] ANCHOS = {12, 12, 10, 20, 14, 28, 16, 36, 24, 16, 26};

    /** @param medico nulo si el reporte es de todos; en ese caso se agrega la columna Médico. */
    public Descarga generar(List<Cita> citas, Medico medico, RangoConsulta rango, String usuario) {
        LocalDate desde = rango.inicio().toLocalDate();
        LocalDate hasta = rango.fin().minusNanos(1).toLocalDate();
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Citas");
            CellStyle titulo = negrita(libro, 14);
            CellStyle encabezado = relleno(libro, "#d9d9d9", true);
            Map<EstadoCita, CellStyle> estilos = new EnumMap<>(EstadoCita.class);
            for (EstadoCita estado : EstadoCita.values()) {
                estilos.put(estado, relleno(libro, aclarar(estado.getColor()), false));
            }
            List<String> columnas = new ArrayList<>(COLUMNAS);
            if (medico == null) {
                columnas.add("Médico");
            }
            escribir(hoja, 0, List.of("Reporte de citas"), titulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, COLUMNAS.size() - 1));
            escribir(hoja, 1, List.of("Médico:", medico == null ? "Todos" : nombre(medico)), null);
            escribir(hoja, 2, List.of("Rango:", FECHA.format(desde) + " a " + FECHA.format(hasta)), null);
            escribir(hoja, 3, List.of("Generado:", FECHA_HORA.format(LocalDateTime.now()) + " por " + usuario), null);
            escribir(hoja, 5, columnas, encabezado);
            int fila = 6;
            for (Cita cita : citas) {
                escribir(hoja, fila++, valores(cita, medico == null), estilos.get(cita.getEstado()));
            }
            fila++;
            escribir(hoja, fila++, List.of("Resumen por estado", "Cantidad"), encabezado);
            for (EstadoCita estado : EstadoCita.values()) {
                long cantidad = citas.stream().filter(c -> c.getEstado() == estado).count();
                escribir(hoja, fila, List.of(estado.name()), estilos.get(estado));
                hoja.getRow(fila++).createCell(1).setCellValue(cantidad);
            }
            escribir(hoja, fila, List.of("TOTAL"), negrita(libro, 11));
            hoja.getRow(fila).createCell(1).setCellValue(citas.size());
            for (int i = 0; i < columnas.size(); i++) {
                hoja.setColumnWidth(i, ANCHOS[i] * 256);
            }
            hoja.createFreezePane(0, 6);
            libro.write(salida);
            return new Descarga("citas_" + desde + "_" + hasta + ".xlsx", ArchivoTabular.TIPO_XLSX,
                    salida.toByteArray());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static List<String> valores(Cita c, boolean conMedico) {
        Raza raza = c.getMascota().getRaza();
        List<String> valores = new ArrayList<>(List.of(FECHA.format(c.getFechaInicio()),
                HORA.format(c.getFechaInicio()), HORA.format(c.getFechaFin()), c.getMascota().getNombreMascota(),
                raza == null ? "" : raza.getEspecie(),
                c.getCliente().getNombres() + " " + c.getCliente().getApellidos(),
                c.getCliente().getTelefono() == null ? "" : c.getCliente().getTelefono(),
                c.getMotivo() == null ? "" : c.getMotivo(),
                c.getServicio() == null ? "" : c.getServicio().getNombre(), c.getEstado().name()));
        if (conMedico) {
            valores.add(nombre(c.getMedico()));
        }
        return valores;
    }

    private static String nombre(Medico medico) {
        return medico.getNombres() + " " + medico.getApellidos();
    }

    private static void escribir(Sheet hoja, int numero, List<String> valores, CellStyle estilo) {
        Row fila = hoja.createRow(numero);
        for (int i = 0; i < valores.size(); i++) {
            Cell celda = fila.createCell(i);
            celda.setCellValue(valores.get(i));
            if (estilo != null) {
                celda.setCellStyle(estilo);
            }
        }
    }

    private static CellStyle negrita(XSSFWorkbook libro, int puntos) {
        Font fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) puntos);
        CellStyle estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        return estilo;
    }

    private static CellStyle relleno(XSSFWorkbook libro, String color, boolean negrita) {
        XSSFCellStyle estilo = libro.createCellStyle();
        estilo.setFillForegroundColor(new XSSFColor(java.awt.Color.decode(color), null));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        if (negrita) {
            Font fuente = libro.createFont();
            fuente.setBold(true);
            estilo.setFont(fuente);
        }
        return estilo;
    }

    /** Mezcla con blanco para que el texto negro se lea. */
    private static String aclarar(String color) {
        java.awt.Color base = java.awt.Color.decode(color);
        return String.format("#%02x%02x%02x", aclarar(base.getRed()), aclarar(base.getGreen()),
                aclarar(base.getBlue()));
    }

    private static int aclarar(int canal) {
        return canal + (255 - canal) * 65 / 100;
    }
}
