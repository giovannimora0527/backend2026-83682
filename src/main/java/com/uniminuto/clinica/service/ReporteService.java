package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.ReporteCatalogoRs;
import com.uniminuto.clinica.models.ReporteRs;
import java.util.List;
import java.util.Map;

public interface ReporteService {

    /** Reportes que el rol autenticado puede usar. */
    List<ReporteCatalogoRs> catalogo();

    ReporteRs generar(String codigo, Map<String, String> parametros);

    Descarga exportar(String codigo, String formato, Map<String, String> parametros);
}
