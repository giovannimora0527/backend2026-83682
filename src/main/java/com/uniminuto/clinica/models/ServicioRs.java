package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.enums.TipoServicio;
import java.math.BigDecimal;

public record ServicioRs(Long id, String codigo, String nombre, String descripcion, TipoServicio tipoServicio,
                         Long especializacionId, String especializacionNombre, Integer duracionMinutos,
                         Boolean activo, TarifaVigente tarifaVigente) {

    public record TarifaVigente(Long id, BigDecimal valor, Long impuestoId, BigDecimal porcentajeIva) {
    }

    public static ServicioRs de(Servicio s, Tarifa t) {
        Especializacion e = s.getEspecializacion();
        return new ServicioRs(s.getId(), s.getCodigo(), s.getNombre(), s.getDescripcion(), s.getTipoServicio(),
                e == null ? null : e.getId(), e == null ? null : e.getNombre(), s.getDuracionMinutos(),
                s.getActivo(), t == null ? null : new TarifaVigente(t.getId(), t.getValor(),
                t.getImpuesto().getId(), t.getImpuesto().getPorcentaje()));
    }
}
