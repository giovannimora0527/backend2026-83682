package com.uniminuto.clinica.models;

import com.uniminuto.clinica.reporte.Columna;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReporteRs(String codigo, String titulo, LocalDateTime generadoEn, String generadoPor,
                        Map<String, Object> filtros, List<Columna> columnas, List<Map<String, Object>> filas,
                        Map<String, Object> totales) {
}
