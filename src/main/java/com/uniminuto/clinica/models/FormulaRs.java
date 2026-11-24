package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.entity.enums.ViaAdministracion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Formula con sus medicamentos. */
public record FormulaRs(Long id, String numero, Long citaId, Long medicoId, String medicoNombre, Integer mascotaId,
                        String mascotaNombre, Long clienteId, String clienteNombre, LocalDateTime fecha,
                        String indicacionesGenerales, EstadoFormula estadoFormula, EstadoDespacho estadoDespacho,
                        LocalDateTime fechaConfirmacion, List<Detalle> detalles) {

    public record Detalle(Long id, Long medicamentoId, String medicamentoCodigo,
                          String medicamentoNombre, BigDecimal dosis,
                          String unidadDosis, Integer frecuenciaHoras, Integer duracionDias,
                          ViaAdministracion viaAdministracion, Integer cantidadPrescrita,
                          Integer cantidadDespachada, String indicaciones) {

        public static Detalle de(FormulaMedicaDetalle d) {
            return new Detalle(d.getId(), d.getMedicamento().getId(), d.getMedicamento().getCodigo(),
                    d.getMedicamento().getNombre(), d.getDosis(), d.getUnidadDosis(), d.getFrecuenciaHoras(),
                    d.getDuracionDias(), d.getViaAdministracion(), d.getCantidadPrescrita(),
                    d.getCantidadDespachada(), d.getIndicaciones());
        }
    }

    public static FormulaRs de(FormulaMedica f, List<FormulaMedicaDetalle> detalles) {
        return new FormulaRs(f.getId(), f.getNumero(), f.getCita().getId(), f.getMedico().getId(),
                f.getMedico().getNombres() + " " + f.getMedico().getApellidos(), f.getMascota().getMascotaId(),
                f.getMascota().getNombreMascota(), f.getCliente().getId(),
                f.getCliente().getNombres() + " " + f.getCliente().getApellidos(), f.getFechaGeneracion(),
                f.getIndicacionesGenerales(), f.getEstadoFormula(), f.getEstadoDespacho(),
                f.getFechaConfirmacion(), detalles.stream().map(Detalle::de).toList());
    }
}
