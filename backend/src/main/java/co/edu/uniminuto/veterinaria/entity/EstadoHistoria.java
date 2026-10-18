package co.edu.uniminuto.veterinaria.entity;

/**
 * Estado de una historia clínica.
 */
public enum EstadoHistoria {
    /** Admite nuevas anotaciones y fórmulas. */
    ABIERTA,
    /** Solo lectura: no admite anotaciones ni fórmulas nuevas. */
    CERRADA
}
