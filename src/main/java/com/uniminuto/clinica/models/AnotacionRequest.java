package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

public record AnotacionRequest(Long historiaClinicaId, Long veterinarioId, LocalDateTime fechaHora,
                               String descripcion, String observacion, String diagnosticoEvolucion) { }
