package com.clinicaveterinaria.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnotacionHistoriaDTO {

    private Integer historiaId;
    private Integer medicoId;
    private LocalDateTime fecha;
    private String descripcion;
}