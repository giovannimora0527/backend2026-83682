package com.uniminuto.clinica.catalogo;

import java.time.LocalDateTime;

/**
 * DTO de respuesta de una raza.
 *
 * @param razaId identificador.
 * @param id identificador (alias para compatibilidad con el frontend).
 * @param nombre nombre de la raza.
 * @param especie especie.
 * @param fechaCreacion fecha de creación.
 * @param fechaModificacion fecha de modificación.
 * @param mascotas número de mascotas registradas con esta raza.
 */
public record RazaDto(int razaId, int id, String nombre, String especie, LocalDateTime fechaCreacion,
                      LocalDateTime fechaModificacion, long mascotas) {
}
