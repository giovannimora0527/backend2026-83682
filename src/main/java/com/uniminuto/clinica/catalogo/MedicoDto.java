package com.uniminuto.clinica.catalogo;

/**
 * DTO de respuesta de un médico veterinario.
 *
 * @param id identificador.
 * @param activo si puede recibir citas.
 * @param estado alias de activo (compatibilidad).
 * @param tipoDocumento tipo de documento.
 * @param numeroDocumento número de documento.
 * @param nombres nombres.
 * @param apellidos apellidos.
 * @param telefono teléfono.
 * @param registroProfesional registro profesional.
 * @param especializacionId especialidad.
 * @param especializacion nombre de la especialidad.
 * @param especializacionActiva si la especialidad está activa.
 * @param usuarioId usuario del sistema asociado.
 * @param usuario nombre de usuario asociado.
 * @param citasFuturas citas futuras reservadas.
 * @param citasTotales total de citas.
 */
public record MedicoDto(long id, boolean activo, boolean estado, String tipoDocumento, String numeroDocumento, String nombres,
                        String apellidos, String telefono, String registroProfesional, long especializacionId,
                        String especializacion, boolean especializacionActiva, Long usuarioId, String usuario,
                        long citasFuturas, long citasTotales) {
}
