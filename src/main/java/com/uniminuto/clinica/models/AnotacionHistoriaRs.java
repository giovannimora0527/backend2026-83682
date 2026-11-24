package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medico;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnotacionHistoriaRs {
    private Long id;
    private HistoriaMedica historia;
    private Medico medico;
    private Cita cita;
    private LocalDateTime fecha;
    private String descripcion;
    private String diagnostico;
    private String observaciones;
    private List<FormulaMedica> formulasMedicas = List.of();

    public AnotacionHistoriaRs(AnotacionHistoria anotacion) {
        this.id = anotacion.getId();
        this.historia = anotacion.getHistoria();
        this.medico = anotacion.getMedico();
        this.cita = anotacion.getCita();
        this.fecha = anotacion.getFecha();
        this.descripcion = anotacion.getDescripcion();
        this.diagnostico = anotacion.getDiagnostico();
        this.observaciones = anotacion.getObservaciones();
    }
}
