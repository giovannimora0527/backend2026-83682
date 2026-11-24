package com.uniminuto.clinica.models;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FormulaRq {

      private Long id;

      private Long citaId;

      private Long medicamentoId;

      private String dosis;

      private String indicaciones;

      private LocalDateTime fechaCreacionRegistro;

      private LocalDateTime fechaModificacionRegistro;

      private String frecuencia;

      private String duracion;
}
