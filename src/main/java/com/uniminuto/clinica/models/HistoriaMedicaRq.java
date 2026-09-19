package com.uniminuto.clinica.models;
import lombok.Data;

@Data
public class HistoriaMedicaRq {

    // Es el Id de la historia. Se utiliza al actualizar.
    private Long historiaId;

    // Es el Id del paciente al que pertenece la historia.
    private Integer pacienteId;
}