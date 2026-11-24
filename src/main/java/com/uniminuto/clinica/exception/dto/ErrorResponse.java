package com.uniminuto.clinica.exception.dto;

/**
 * Cuerpo de error devuelto por la API. El frontend muestra el campo {@code mensaje}.
 *
 * @param status  código HTTP.
 * @param error   nombre corto del error.
 * @param mensaje descripción legible para el usuario.
 */
public record ErrorResponse(int status, String error, String mensaje) {
}
