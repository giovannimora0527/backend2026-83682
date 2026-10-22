package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.HistoriaMedica;
import lombok.Data;

import java.util.List;

@Data
public class HistoriaMedicaRs {
    private HistoriaMedica historia;
    private List<AnotacionHistoriaRs> anotaciones;
}
