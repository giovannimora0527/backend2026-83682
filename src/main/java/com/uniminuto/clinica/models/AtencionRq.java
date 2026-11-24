package com.uniminuto.clinica.models;

import jakarta.validation.Valid;
import java.util.List;

/** Borrador: si servicios no llega, se conservan los registrados. */
public record AtencionRq(String motivoConsulta, String sintomas, String diagnostico, String observaciones,
                         String recomendaciones, @Valid List<AtencionServicioRq> servicios) {
}
