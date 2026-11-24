package com.uniminuto.clinica.reporte;

import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.pdf.DocumentoPdf;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Exporta cualquier reporte; apaisado si tiene muchas columnas. */
@Component
public class PdfExporter {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    public byte[] exportar(ConfiguracionRs clinica, ReporteRs reporte) {
        List<Columna> columnas = reporte.columnas();
        String filtros = reporte.filtros().entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("   "));
        DocumentoPdf pdf = new DocumentoPdf(clinica, reporte.titulo(), (filtros.isEmpty() ? "" : filtros + "   ")
                + "Generado: " + FECHA_HORA.format(reporte.generadoEn()) + " por " + reporte.generadoPor(), null,
                columnas.size() > 6);
        List<List<String>> filas = new ArrayList<>(reporte.filas().stream().map(f -> fila(columnas, f)).toList());
        boolean conTotales = !reporte.totales().isEmpty();
        if (conTotales) {
            List<String> total = new ArrayList<>(fila(columnas, reporte.totales()));
            if (total.getFirst().isEmpty()) {
                total.set(0, "TOTAL");
            }
            filas.add(total);
        }
        Set<Integer> derecha = new HashSet<>();
        for (int i = 0; i < columnas.size(); i++) {
            if (columnas.get(i).tipo().esNumerica()) {
                derecha.add(i);
            }
        }
        List<String> titulos = columnas.stream().map(Columna::titulo).toList();
        float puntos = columnas.size() > 9 ? 6.5f : 8;
        pdf.tabla(titulos, pdf.anchos(titulos, filas, puntos), filas, derecha, conTotales, puntos);
        if (reporte.filas().isEmpty()) {
            pdf.texto("No hay datos para los filtros seleccionados.");
        }
        return pdf.terminar("reporte").contenido();
    }

    private static List<String> fila(List<Columna> columnas, Map<String, Object> datos) {
        return columnas.stream().map(c -> texto(c, datos.get(c.clave()))).toList();
    }

    private static String texto(Columna columna, Object valor) {
        if (valor == null) {
            return "";
        }
        return switch (valor) {
            case LocalDateTime f -> FECHA_HORA.format(f);
            case LocalDate f -> FECHA.format(f);
            case Number n when columna.tipo() == TipoColumna.MONEDA -> DocumentoPdf.dinero(new BigDecimal(n.toString()));
            case Number n when columna.tipo() == TipoColumna.PORCENTAJE ->
                    DocumentoPdf.numero(new BigDecimal(n.toString())) + " %";
            case Number n -> DocumentoPdf.numero(new BigDecimal(n.toString()));
            // El espacio permite partir los nombres largos, como SALIDA_DESPACHO.
            default -> valor.toString().replace('_', ' ');
        };
    }
}
