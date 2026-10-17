package com.uniminuto.clinica.exception;

/**
 * Excepcion personalizada que se lanza cuando el usuario envia un rango de
 * fechas invalido, por ejemplo cuando la fecha inicial es posterior a la
 * fecha final.
 */
public class FechaInvalidaException extends RuntimeException {

    /**
     * Crea la excepcion con un mensaje que explica por que el rango de
     * fechas no es valido.
     *
     * @param mensaje descripcion del error
     */
    public FechaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
