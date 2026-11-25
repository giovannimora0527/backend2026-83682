package com.uniminuto.clinica.catalogo;

import java.time.LocalDateTime;

/**
 * DTO de respuesta de una mascota con su raza, su propietario y sus conteos clínicos.
 *
 * @param mascotaId identificador.
 * @param nombreMascota nombre.
 * @param edad edad en años.
 * @param activo estado.
 * @param fechaRegistro fecha de registro.
 * @param fechaModificacion fecha de modificación.
 * @param razaId raza.
 * @param clienteId propietario.
 * @param citasFuturas citas futuras reservadas.
 * @param citasTotales total de citas.
 * @param historias historias clínicas.
 * @param raza datos de la raza.
 * @param cliente datos del propietario.
 */
public record MascotaDto(int mascotaId, String nombreMascota, int edad, boolean activo, LocalDateTime fechaRegistro,
                         LocalDateTime fechaModificacion, int razaId, long clienteId, long citasFuturas, long citasTotales,
                         long historias, RazaResumen raza, ClienteResumen cliente) {

    /**
     * Raza de la mascota.
     *
     * @param id identificador.
     * @param razaId identificador (alias).
     * @param nombre nombre.
     * @param especie especie.
     * @param fechaCreacion fecha de creación de la raza.
     */
    public record RazaResumen(int id, int razaId, String nombre, String especie, LocalDateTime fechaCreacion) {
    }

    /**
     * Propietario de la mascota.
     *
     * @param clienteId identificador.
     * @param tipoDocumento tipo de documento.
     * @param numeroDocumento número de documento.
     * @param nombres nombres.
     * @param apellidos apellidos.
     * @param telefono teléfono.
     * @param activo false si está archivado.
     */
    public record ClienteResumen(long clienteId, String tipoDocumento, String numeroDocumento, String nombres,
                                 String apellidos, String telefono, boolean activo) {
    }
}
