package co.edu.uniminuto.veterinaria.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista COMPLETA de una fórmula (botón "Ver").
 *
 * @param id                      identificador
 * @param historiaId              historia clínica asociada
 * @param citaId                  cita de la consulta
 * @param fechaConsulta           fecha y hora de la consulta
 * @param motivoConsulta          motivo de la consulta
 * @param clienteId               identificador del cliente
 * @param clienteNombre           nombre completo del cliente
 * @param mascotaId               identificador de la mascota
 * @param mascotaNombre           nombre de la mascota
 * @param veterinarioId           identificador del veterinario
 * @param veterinarioNombre       nombre completo del veterinario
 * @param medicamentoId           identificador del medicamento
 * @param medicamentoNombre       nombre del medicamento
 * @param medicamentoPresentacion presentación del medicamento
 * @param fechaEmision            fecha de emisión
 * @param dosisValor              cantidad de la dosis
 * @param dosisUnidad             unidad de la dosis
 * @param frecuenciaHoras         frecuencia en horas
 * @param duracionDias            duración en días
 * @param indicaciones            indicaciones
 * @param activo                  si la fórmula está vigente
 * @param fechaCreacion           fecha de creación del registro
 * @param fechaActualizacion      fecha de la última actualización
 */
public record FormulaDetalleResponse(Long id, Long historiaId, Long citaId, LocalDateTime fechaConsulta,
                                     String motivoConsulta, Long clienteId, String clienteNombre, Long mascotaId,
                                     String mascotaNombre, Long veterinarioId, String veterinarioNombre,
                                     Long medicamentoId, String medicamentoNombre, String medicamentoPresentacion,
                                     LocalDate fechaEmision, BigDecimal dosisValor, String dosisUnidad,
                                     Integer frecuenciaHoras, Integer duracionDias, String indicaciones,
                                     boolean activo, LocalDateTime fechaCreacion,
                                     LocalDateTime fechaActualizacion) {
}
