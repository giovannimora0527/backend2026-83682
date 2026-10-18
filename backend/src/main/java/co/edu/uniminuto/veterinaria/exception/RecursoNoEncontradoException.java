package co.edu.uniminuto.veterinaria.exception;

/**
 * Se lanza cuando un registro solicitado o referenciado no existe (HTTP 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepción con un mensaje comprensible para el usuario.
     *
     * @param mensaje descripción del registro que no se encontró
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
