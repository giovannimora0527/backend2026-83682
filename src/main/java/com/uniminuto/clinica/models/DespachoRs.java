package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** estado es el del despacho; estadoDespacho, el actual de la formula. */
public record DespachoRs(Long id, String numero, Long formulaId, String formulaNumero, LocalDateTime fecha,
                         LocalDateTime fechaInicio, String mascotaNombre, String clienteNombre, String medicoNombre,
                         String usuario, EstadoPreparacion estado, EstadoDespacho estadoDespacho, String observacion,
                         boolean revertido, LocalDateTime fechaReversion, String usuarioReversion,
                         String motivoReversion, List<Item> items, List<String> sinExistencias) {

    public record Item(Long formulaDetalleId, Long medicamentoId, String medicamentoNombre, Long loteId,
                       String numeroLote, LocalDate fechaVencimiento, Integer cantidad, Boolean facturado) {
    }
}
