package com.uniminuto.clinica.catalogo;

/**
 * DTO de una especialidad veterinaria.
 *
 * @param id identificador.
 * @param nombre nombre.
 * @param descripcion descripción.
 * @param codigoEspecializacion código.
 * @param activo estado.
 */
public record EspecializacionDto(long id, String nombre, String descripcion, String codigoEspecializacion, boolean activo) {
}
