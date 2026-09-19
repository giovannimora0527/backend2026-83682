package com.clinicaveterinaria.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EspecializacionDTO {

    private String nombre;
    private String descripcion;
    private String codigoEspecializacion;
}