package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

public record AnotacionResponse(Long anotacionId, Long historiaClinicaId, Long veterinarioId,
                                String veterinarioNombre, LocalDateTime fechaHora, String descripcion,
                                String observacion, String diagnosticoEvolucion) { }
