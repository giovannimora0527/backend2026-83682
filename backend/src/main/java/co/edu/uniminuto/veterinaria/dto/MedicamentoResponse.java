package co.edu.uniminuto.veterinaria.dto;

import java.time.LocalDate;

/**
 * Datos de un medicamento expuestos por la API.
 *
 * @param id           identificador
 * @param nombre       nombre
 * @param presentacion presentación
 * @param fechaVence   fecha de vencimiento
 */
public record MedicamentoResponse(Long id, String nombre, String presentacion, LocalDate fechaVence) {
}
