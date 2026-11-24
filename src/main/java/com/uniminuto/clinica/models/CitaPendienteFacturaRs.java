package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

public record CitaPendienteFacturaRs(Long citaId, LocalDateTime fecha, Long clienteId, String clienteNombre,
                                     String clienteDocumento, Integer mascotaId, String mascotaNombre,
                                     String medicoNombre, String motivo, boolean despachoPendiente) {
}
