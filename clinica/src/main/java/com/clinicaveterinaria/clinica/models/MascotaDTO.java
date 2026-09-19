package com.clinicaveterinaria.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {

    private String nombreMascota;
    private Integer edad;
    private Integer razaId;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;
    private Integer clienteId;
}