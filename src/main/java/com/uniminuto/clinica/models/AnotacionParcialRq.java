package com.uniminuto.clinica.models;
/** Nota que un veterinario agrega al historial. */
public record AnotacionParcialRq(Long medicoId, String descripcion, String observacion,
                                 String evolucion) {}
