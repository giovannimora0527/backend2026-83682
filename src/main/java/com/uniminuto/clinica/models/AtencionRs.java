package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.entity.AtencionServicio;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import java.time.LocalDateTime;
import java.util.List;

public record AtencionRs(Long id, Long citaId, Long medicoId, String medicoNombre, EstadoCita estadoCita,
                         String motivoConsulta, String sintomas, String diagnostico, String observaciones,
                         String recomendaciones, List<ServicioPrestado> servicios, LocalDateTime fechaInicio,
                         LocalDateTime fechaFin, Boolean finalizada) {

    public record ServicioPrestado(Long servicioId, String servicioCodigo, String servicioNombre, Integer cantidad) {
    }

    public static AtencionRs de(AtencionMedica a, List<AtencionServicio> servicios) {
        return new AtencionRs(a.getId(), a.getCita().getId(), a.getMedico().getId(),
                a.getMedico().getNombres() + " " + a.getMedico().getApellidos(), a.getCita().getEstado(),
                a.getMotivoConsulta(), a.getSintomas(), a.getDiagnostico(), a.getObservaciones(),
                a.getRecomendaciones(), servicios.stream().map(s -> new ServicioPrestado(s.getServicio().getId(),
                s.getServicio().getCodigo(), s.getServicio().getNombre(), s.getCantidad())).toList(),
                a.getFechaInicio(), a.getFechaFin(), a.getFinalizada());
    }
}
