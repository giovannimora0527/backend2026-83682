package com.uniminuto.clinica.models;

import java.util.List;

public record DisponibilidadRs(Long medicamentoId, int cantidadDisponibleVigente, int cantidadVencida,
                               List<LoteRs> lotesVigentes) {
}
