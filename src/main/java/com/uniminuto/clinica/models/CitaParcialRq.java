package com.uniminuto.clinica.models;
import java.time.LocalDateTime;
/** Información mínima necesaria para programar una cita. */
public record CitaParcialRq(Long clienteId, Integer mascotaId, Long medicoId,
                             LocalDateTime fechaHora, String motivo, String estado) {}
