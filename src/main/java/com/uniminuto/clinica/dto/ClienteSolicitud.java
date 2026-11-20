package com.uniminuto.clinica.dto;

import java.time.LocalDate;

public record ClienteSolicitud(String tipoDocumento, String numeroDocumento, String nombres,
        String apellidos, LocalDate fechaNacimiento, String genero, String telefono, String direccion) {}
