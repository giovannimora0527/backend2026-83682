package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

public interface AnotacionHistoriaService {

    MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq);
}
