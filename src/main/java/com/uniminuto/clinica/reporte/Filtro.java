package com.uniminuto.clinica.reporte;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record Filtro(String clave, boolean obligatorio,
                     @JsonInclude(JsonInclude.Include.NON_NULL) List<String> opciones) {

    public static final Filtro DESDE = new Filtro("desde", true, null);
    public static final Filtro HASTA = new Filtro("hasta", true, null);
    public static final Filtro DESDE_OPCIONAL = new Filtro("desde", false, null);
    public static final Filtro HASTA_OPCIONAL = new Filtro("hasta", false, null);
    public static final Filtro AGRUPACION = new Filtro("agrupacion", false, List.of("DIA", "MES", "ANIO"));

    public static Filtro opcional(String clave) {
        return new Filtro(clave, false, null);
    }

    public static Filtro obligatorio(String clave) {
        return new Filtro(clave, true, null);
    }

    public static Filtro opciones(String clave, List<String> opciones) {
        return new Filtro(clave, false, opciones);
    }
}
