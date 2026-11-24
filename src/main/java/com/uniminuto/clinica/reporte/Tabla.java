package com.uniminuto.clinica.reporte;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Contenido de un reporte. Los totales usan las claves de las columnas. */
public record Tabla(List<Columna> columnas, List<Map<String, Object>> filas, Map<String, Object> totales) {

    public static Tabla de(List<Columna> columnas, List<Map<String, Object>> filas, String... aTotalizar) {
        Map<String, Object> totales = new LinkedHashMap<>();
        for (String clave : aTotalizar) {
            totales.put(clave, filas.stream().map(f -> f.get(clave)).filter(Objects::nonNull)
                    .map(v -> v instanceof BigDecimal b ? b : new BigDecimal(v.toString()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return new Tabla(columnas, filas, totales);
    }

    /** Fila con los valores en el orden de las columnas. */
    public static Map<String, Object> fila(List<Columna> columnas, Object... valores) {
        if (columnas.size() != valores.length) {
            throw new IllegalArgumentException("La fila no tiene los valores de todas las columnas.");
        }
        Map<String, Object> fila = new LinkedHashMap<>();
        for (int i = 0; i < valores.length; i++) {
            Object v = valores[i];
            if (v != null && columnas.get(i).tipo() == TipoColumna.MONEDA) {
                v = new BigDecimal(v.toString()).setScale(2, RoundingMode.HALF_UP);
            } else if (v instanceof Enum<?> e) {
                v = e.name();
            }
            fila.put(columnas.get(i).clave(), v);
        }
        return fila;
    }
}
