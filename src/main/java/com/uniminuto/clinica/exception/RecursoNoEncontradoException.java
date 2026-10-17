package com.uniminuto.clinica.exception;

/**
 * Excepcion personalizada que se lanza cuando se busca un registro por id
 * (por ejemplo un paciente, una cita o una historia medica) y este no
 * existe en la base de datos.
 *
 * <p>Al extender de {@link RuntimeException} no es obligatorio declararla
 * con "throws" en cada metodo, lo que mantiene el codigo mas limpio y
 * simple de leer.</p>
 */
public class RecursoNoEncontradoException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje que explica que recurso no se
     * encontro.
     *
     * @param mensaje descripcion del error, por ejemplo
     *                "No se encontro la cita con id 5"
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
