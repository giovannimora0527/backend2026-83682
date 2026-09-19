package com.clinicaveterinaria.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormulaMedicaDTO {

    private Integer citaId;
    private Integer medicamentoId;
    private String dosis;
    private String indicaciones;
    private LocalDateTime fechaCreacionRegistro;
    private LocalDateTime fechaActualizacionRegistro;
}