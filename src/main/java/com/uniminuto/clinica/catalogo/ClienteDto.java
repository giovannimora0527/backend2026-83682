package com.uniminuto.clinica.catalogo;

import java.time.LocalDate;

/**
 * DTO de respuesta de un cliente (propietario).
 *
 * @param clienteId identificador.
 * @param usuarioId usuario del sistema asociado (opcional).
 * @param tipoDocumento tipo de documento.
 * @param numeroDocumento número de documento.
 * @param nombres nombres.
 * @param apellidos apellidos.
 * @param fechaNacimiento fecha de nacimiento.
 * @param genero M, F u O.
 * @param telefono teléfono.
 * @param direccion dirección.
 * @param activo false si está archivado.
 */
public record ClienteDto(long clienteId, Long usuarioId, String tipoDocumento, String numeroDocumento, String nombres,
                         String apellidos, LocalDate fechaNacimiento, String genero, String telefono, String direccion,
                         boolean activo) {
}
