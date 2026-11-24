package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.AtencionCitaRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.AtencionRs;
import com.uniminuto.clinica.models.Descarga;

public interface AtencionService {

    AtencionRs iniciar(Long citaId);

    AtencionCitaRs consultar(Long citaId);

    AtencionRs guardarBorrador(Long id, AtencionRq rq);

    AtencionRs finalizar(Long id);

    Descarga prefactura(Long id);
}
