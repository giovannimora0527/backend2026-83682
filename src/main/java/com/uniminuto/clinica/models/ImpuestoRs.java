package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.Impuesto;
import java.math.BigDecimal;

public record ImpuestoRs(Long id, String nombre, BigDecimal porcentaje, Boolean activo) {

    public static ImpuestoRs de(Impuesto i) {
        return new ImpuestoRs(i.getId(), i.getNombre(), i.getPorcentaje(), i.getActivo());
    }
}
