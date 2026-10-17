package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class MedicoRq {

    /**
     * ID del médico (necesario para actualizar).
     */
    private Long id;

    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String registroProfesional;
    // Agrega aquí otros campos que tenga tu entidad Medico (ej: especialidad)
}