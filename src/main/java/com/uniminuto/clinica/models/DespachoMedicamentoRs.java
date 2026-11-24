package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
import java.time.LocalDateTime;

/** Dispensacion de un medicamento y los movimientos que la respaldan. */
public record DespachoMedicamentoRs(Long despachoId, String despachoNumero, Long formulaId, String formulaNumero,
                                    LocalDateTime fecha, String usuario, String medicoNombre, String clienteNombre,
                                    String mascotaNombre, Long loteId, String numeroLote, Integer cantidad,
                                    EstadoPreparacion estado, Long movimientoId, Integer saldoLote, Integer saldo,
                                    Long movimientoReversionId) {
}
