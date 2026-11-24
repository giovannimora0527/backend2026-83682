package com.uniminuto.clinica.entity.enums;

import java.util.Set;

/** Estados y transiciones de C5. ATENDIDA y CANCELADA son finales. */
public enum EstadoCita {
    PROGRAMADA("#f0ad4e"),
    CONFIRMADA("#0d6efd"),
    EN_ATENCION("#6f42c1"),
    ATENDIDA("#198754"),
    CANCELADA("#6c757d");

    private final String color;

    EstadoCita(String color) {
        this.color = color;
    }

    /** Color en el calendario y en el reporte. */
    public String getColor() {
        return color;
    }

    public boolean puedePasarA(EstadoCita nuevo) {
        return switch (this) {
            case PROGRAMADA -> Set.of(CONFIRMADA, EN_ATENCION, CANCELADA).contains(nuevo);
            case CONFIRMADA -> Set.of(EN_ATENCION, CANCELADA).contains(nuevo);
            case EN_ATENCION -> nuevo == ATENDIDA;
            case ATENDIDA, CANCELADA -> false;
        };
    }

    /** Solo estas citas se editan o reprograman. */
    public boolean esEditable() {
        return this == PROGRAMADA || this == CONFIRMADA;
    }
}
