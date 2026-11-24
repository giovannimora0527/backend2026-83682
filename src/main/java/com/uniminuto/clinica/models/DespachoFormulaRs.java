package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.ViaAdministracion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Preparacion: despachoId es nulo si no hay una preparacion abierta. */
public record DespachoFormulaRs(Long formulaId, Long despachoId, String despachoNumero, String usuarioPreparacion,
                                String numero, LocalDateTime fecha, String medicoNombre, String clienteNombre,
                                String clienteDocumento, String mascotaNombre, String indicacionesGenerales,
                                EstadoDespacho estadoDespacho, List<Detalle> detalles) {

    public record Detalle(Long formulaDetalleId, Long medicamentoId, String codigo, String nombre, BigDecimal dosis,
                          String unidadDosis, Integer frecuenciaHoras, Integer duracionDias,
                          ViaAdministracion viaAdministracion, String indicaciones, Integer cantidadPrescrita,
                          Integer cantidadDespachada, Integer cantidadPendiente, Integer existenciaTotalVigente,
                          boolean puedeEntregarse, List<Lote> lotes) {
    }

    /** FEFO: vigentes por vencimiento y, al final, los vencidos con sugerencia 0. */
    public record Lote(Long loteId, String numeroLote, LocalDate fechaVencimiento, Integer cantidadDisponible,
                       boolean vencido, boolean proximoVencer, int cantidadSugerida) {
    }
}
