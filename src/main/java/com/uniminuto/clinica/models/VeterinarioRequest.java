package com.uniminuto.clinica.models;

/**
 * Datos para crear o actualizar un médico (tabla {@code medico}).
 * Cada campo corresponde a una columna existente; {@code especializacionId} es {@code especializacion_id}.
 */
public record VeterinarioRequest(String tipoDocumento, String numeroDocumento, String nombres, String apellidos,
                                 String telefono, String registroProfesional, Integer especializacionId) { }
