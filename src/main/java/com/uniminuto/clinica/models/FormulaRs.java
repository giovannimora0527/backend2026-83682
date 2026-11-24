package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.entity.enums.ViaAdministracion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/** Formula con sus medicamentos. numero y estadoDespacho son nulos en BORRADOR. */
public record FormulaRs(Long id, String numero, Long citaId, Long atencionId, Long medicoId, String medicoNombre,
                        Integer mascotaId, String mascotaNombre, Long clienteId, String clienteNombre,
                        LocalDateTime fecha, String indicacionesGenerales, EstadoFormula estadoFormula,
                        EstadoDespacho estadoDespacho, LocalDateTime fechaConfirmacion, String motivoAnulacion,
                        List<Detalle> detalles) {

    /** cantidadDisponibleVigente es informativa: no impide formular. */
    public record Detalle(Long id, Long medicamentoId, String medicamentoCodigo, String medicamentoNombre,
                          String presentacion, BigDecimal dosis, String unidadDosis, Integer frecuenciaHoras,
                          Integer duracionDias, ViaAdministracion viaAdministracion, Integer cantidadSugerida,
                          Integer cantidadPrescrita, Integer cantidadDespachada, String indicaciones,
                          Integer cantidadDisponibleVigente, Boolean alcanza) {

        public static Detalle de(FormulaMedicaDetalle d, int disponible) {
            return new Detalle(d.getId(), d.getMedicamento().getId(), d.getMedicamento().getCodigo(),
                    d.getMedicamento().getNombre(), d.getMedicamento().getPresentacion(), d.getDosis(),
                    d.getUnidadDosis(), d.getFrecuenciaHoras(), d.getDuracionDias(), d.getViaAdministracion(),
                    sugerida(d.getDosis(), d.getFrecuenciaHoras(), d.getDuracionDias()), d.getCantidadPrescrita(),
                    d.getCantidadDespachada(), d.getIndicaciones(), disponible,
                    disponible >= d.getCantidadPrescrita() - d.getCantidadDespachada());
        }
    }

    public static int sugerida(BigDecimal dosis, int frecuenciaHoras, int duracionDias) {
        return Math.max(1, dosis.multiply(BigDecimal.valueOf(24L * duracionDias))
                .divide(BigDecimal.valueOf(frecuenciaHoras), 0, RoundingMode.CEILING).intValueExact());
    }

    public static FormulaRs de(FormulaMedica f, List<Detalle> detalles) {
        return new FormulaRs(f.getId(), f.getNumero(), f.getCita().getId(),
                f.getAtencion() == null ? null : f.getAtencion().getId(), f.getMedico().getId(),
                f.getMedico().getNombres() + " " + f.getMedico().getApellidos(), f.getMascota().getMascotaId(),
                f.getMascota().getNombreMascota(), f.getCliente().getId(),
                f.getCliente().getNombres() + " " + f.getCliente().getApellidos(), f.getFechaGeneracion(),
                f.getIndicacionesGenerales(), f.getEstadoFormula(), f.getEstadoDespacho(),
                f.getFechaConfirmacion(), f.getMotivoAnulacion(), detalles);
    }
}
