package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.enums.EstadoCita;
import java.time.LocalDateTime;
import java.util.List;

/** atencion es nula si todavia no se ha iniciado. Los antecedentes van del mas reciente al mas antiguo. */
public record AtencionCitaRs(AtencionRs atencion, CitaResumen cita, MascotaResumen mascota, Propietario propietario,
                             Antecedentes antecedentes) {

    public record CitaResumen(Long id, LocalDateTime fechaInicio, LocalDateTime fechaFin, EstadoCita estado,
                              String motivo, Long servicioId, String servicioNombre) {
    }

    public record MascotaResumen(Integer id, String nombre, String especie, String raza, Integer edad,
                                 Boolean activo) {
    }

    public record Propietario(Long id, String nombre, String documento, String telefono, String correo) {
    }

    public record Antecedentes(List<AtencionRs> atenciones, List<Anotacion> anotaciones, List<FormulaRs> formulas) {
    }

    /** Anotaciones de la historia que no provienen de una atencion. */
    public record Anotacion(Long id, LocalDateTime fecha, String medicoNombre, String descripcion,
                            String diagnostico, String observaciones) {
    }
}
