package com.uniminuto.clinica.models;

/**
 * Enumeracion (lista fija de valores permitidos) que representa en que
 * estado puede estar una {@link Cita}.
 *
 * <p>Usar un enum en vez de un simple texto libre evita errores como
 * escribir "pendiente", "Pendiente" o "PENDIENTE" en distintos lugares del
 * codigo: solo se pueden usar los valores definidos aqui.</p>
 */
public enum EstadoCita {

    /** La cita fue creada pero todavia no ha ocurrido. */
    PENDIENTE,

    /** La cita fue confirmada por la clinica o el cliente. */
    CONFIRMADA,

    /** La cita ya se realizo. */
    COMPLETADA,

    /** La cita fue cancelada y no se va a realizar. */
    CANCELADA
}
