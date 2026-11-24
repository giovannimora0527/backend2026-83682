package com.uniminuto.clinica.models;

import com.uniminuto.clinica.reporte.Filtro;
import java.util.List;

public record ReporteCatalogoRs(String codigo, String titulo, String grupo, List<Filtro> filtros) {
}
