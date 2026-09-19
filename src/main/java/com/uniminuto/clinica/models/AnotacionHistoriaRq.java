package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class AnotacionHistoriaRq {

    // Id de la anotación. Se utiliza cuando vamos a actualizar.
    private Long anotacionId;

    // Id de la historia médica a la que pertenece la anotación.
    private Integer historiaId;

    // Id del médico que realiza la anotación.
    private Integer medicoId;

    // Información escrita por el médico.
    private String descripcion;
}