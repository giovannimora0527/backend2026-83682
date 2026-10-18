package co.edu.uniminuto.veterinaria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista RESUMIDA de una fórmula para el listado (no incluye indicaciones, historia ni fechas de auditoría).
 *
 * @param id               identificador
 * @param fechaCreacion    fecha y hora de creación del registro (criterio de orden inicial del listado)
 * @param fechaEmision     fecha de emisión
 * @param mascotaNombre    nombre de la mascota
 * @param clienteNombre    nombre completo del cliente
 * @param veterinarioNombre nombre completo del veterinario
 * @param medicamentoNombre nombre del medicamento
 * @param posologia        texto resumido de dosis, frecuencia y duración
 * @param activo           si la fórmula está vigente
 */
public record FormulaResumenResponse(Long id, LocalDateTime fechaCreacion, LocalDate fechaEmision,
                                     String mascotaNombre, String clienteNombre, String veterinarioNombre,
                                     String medicamentoNombre, String posologia, boolean activo) {
}
