package co.edu.uniminuto.veterinaria.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se incumple una regla de negocio.
 * <ul>
 *   <li>409 Conflict: el estado actual de los datos impide la operación (cruce de horarios, cita ya cancelada...).</li>
 *   <li>422 Unprocessable Entity: los datos enviados son coherentes en formato pero inválidos para el negocio
 *       (mascota de otro cliente, fecha pasada...).</li>
 * </ul>
 */
public class ReglaNegocioException extends RuntimeException {

    /** Código HTTP que debe devolverse. */
    private final HttpStatus estado;

    /**
     * Crea la excepción.
     *
     * @param estado  código HTTP a devolver
     * @param mensaje mensaje comprensible para el usuario
     */
    public ReglaNegocioException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    /**
     * Crea una excepción de conflicto (409).
     *
     * @param mensaje mensaje para el usuario
     * @return excepción lista para lanzar
     */
    public static ReglaNegocioException conflicto(String mensaje) {
        return new ReglaNegocioException(HttpStatus.CONFLICT, mensaje);
    }

    /**
     * Crea una excepción de datos inválidos para el negocio (422).
     *
     * @param mensaje mensaje para el usuario
     * @return excepción lista para lanzar
     */
    public static ReglaNegocioException invalida(String mensaje) {
        return new ReglaNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, mensaje);
    }

    /**
     * Devuelve el código HTTP asociado.
     *
     * @return estado HTTP
     */
    public HttpStatus getEstado() {
        return estado;
    }
}
