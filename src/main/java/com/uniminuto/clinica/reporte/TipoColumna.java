package com.uniminuto.clinica.reporte;

import com.fasterxml.jackson.annotation.JsonValue;

/** El nombre en el JSON es el del contrato. */
public enum TipoColumna {
    TEXTO("texto"),
    NUMERO("numero"),
    MONEDA("moneda"),
    FECHA("fecha"),
    FECHA_HORA("fechaHora"),
    PORCENTAJE("porcentaje");

    private final String json;

    TipoColumna(String json) {
        this.json = json;
    }

    @JsonValue
    public String getJson() {
        return json;
    }

    public boolean esNumerica() {
        return this == NUMERO || this == MONEDA || this == PORCENTAJE;
    }
}
