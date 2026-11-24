package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CitaRq {

    private Long id;
    private Integer mascotaId;
    private Long medicoId;
    @JsonAlias("fechaInicio")
    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;

    /** Opcional: define la duracion. Al actualizar, si no llega se conserva el actual. */
    private Long servicioId;

    /** Opcional: debe ser el dueño de la mascota. */
    private Long clienteId;

    /** Obligatorio si el estado es CANCELADA. */
    private String motivoCancelacion;
}
