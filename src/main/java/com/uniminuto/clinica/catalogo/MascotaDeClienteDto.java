package com.uniminuto.clinica.catalogo;

/**
 * DTO de una mascota activa de un cliente (combo dependiente cliente → mascota).
 *
 * @param mascotaId identificador.
 * @param nombreMascota nombre.
 * @param edad edad en años.
 * @param raza nombre de la raza.
 * @param especie especie.
 * @param clienteId propietario.
 */
public record MascotaDeClienteDto(long mascotaId, String nombreMascota, int edad, String raza, String especie, long clienteId) {
}
