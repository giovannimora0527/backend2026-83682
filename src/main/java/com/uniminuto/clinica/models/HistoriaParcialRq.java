package com.uniminuto.clinica.models;
/** Datos de la consulta médica relacionados con una cita atendida. */
public record HistoriaParcialRq(Integer mascotaId, Long citaId, Long medicoId,
                                String motivoConsulta, String diagnostico, String observaciones) {}
