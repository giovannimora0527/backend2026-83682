package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class MedicoRq {

    private Long id;

    private String tipoDocumento;

    private String numeroDocumento;

    private String nombres;

    private String apellidos;

    private String telefono;

    private String registroProfesional;

    private Long especializacionId;

    /** Opcional: usuario con rol MEDICO. Si no llega, se conserva el actual. */
    private Long usuarioId;
}
