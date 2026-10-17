package com.uniminuto.clinica.models;
import java.time.LocalDateTime;
/** Tratamiento indicado después de una consulta veterinaria. */
public record FormulaParcialRq(Long citaId, Long medicamentoId, String dosis,
    String frecuencia, String duracionTratamiento, String indicaciones,
    LocalDateTime fechaEmision) {}
