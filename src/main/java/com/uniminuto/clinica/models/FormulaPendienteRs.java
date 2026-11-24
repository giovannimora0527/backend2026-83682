package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Bandeja de despacho. */
public record FormulaPendienteRs(Long formulaId, String numero, LocalDateTime fecha, String medicoNombre,
                                 String clienteNombre, String mascotaNombre, int totalMedicamentos,
                                 String indicacionesGenerales, EstadoDespacho estadoDespacho,
                                 String usuarioPreparacion, List<Formulado> medicamentos) {

    public record Formulado(String nombre, BigDecimal dosis, String unidadDosis, Integer frecuenciaHoras,
                              Integer duracionDias, String indicaciones, Integer cantidadPrescrita,
                              Integer cantidadDespachada) {
    }
}
