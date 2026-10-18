package co.edu.uniminuto.veterinaria.entity;

/**
 * Estados posibles de una cita y sus transiciones permitidas.
 * <p>
 * Justificación de las restricciones: una cita atendida es información histórica y una cita cancelada
 * no debe comportarse como activa, por eso ambos son estados terminales. Una cancelada solo vuelve a
 * estar activa mediante la operación explícita de reactivación.
 */
public enum EstadoCita {
    /** Cita creada, sin confirmar. */
    PENDIENTE("Pendiente"),
    /** Cita confirmada por el cliente. */
    CONFIRMADA("Confirmada"),
    /** Cita ya realizada (estado terminal, genera historia clínica). */
    ATENDIDA("Atendida"),
    /** Cita cancelada (estado terminal, se reactiva solo de forma explícita). */
    CANCELADA("Cancelada"),
    /** Cita programada en agenda. */
    PROGRAMADA("Programada");

    /** Texto legible para mostrar en mensajes. */
    private final String etiqueta;

    /**
     * Crea un estado con su etiqueta legible.
     *
     * @param etiqueta texto para el usuario
     */
    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /**
     * Devuelve el texto legible del estado.
     *
     * @return etiqueta del estado
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Indica si una cita en este estado puede pasar al estado destino.
     *
     * @param destino estado al que se quiere pasar
     * @return {@code true} si la transición es válida (o si no hay cambio)
     */
    public boolean puedePasarA(EstadoCita destino) {
        if (this == destino) {
            return true;
        }
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == PROGRAMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == PROGRAMADA || destino == ATENDIDA || destino == CANCELADA;
            case PROGRAMADA -> destino == CONFIRMADA || destino == ATENDIDA || destino == CANCELADA;
            case ATENDIDA, CANCELADA -> false;
        };
    }
}
