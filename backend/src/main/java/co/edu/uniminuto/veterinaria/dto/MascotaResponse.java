package co.edu.uniminuto.veterinaria.dto;

/**
 * Datos de una mascota expuestos por la API.
 *
 * @param id            identificador
 * @param nombre        nombre de la mascota
 * @param edad          edad en años
 * @param razaId        identificador de la raza
 * @param raza          nombre de la raza
 * @param especie       especie
 * @param clienteId     identificador del dueño
 * @param clienteNombre nombre completo del dueño
 * @param activo        si la mascota está activa
 */
public record MascotaResponse(Long id, String nombre, Integer edad, Long razaId, String raza, String especie,
                              Long clienteId, String clienteNombre, boolean activo) {
}
