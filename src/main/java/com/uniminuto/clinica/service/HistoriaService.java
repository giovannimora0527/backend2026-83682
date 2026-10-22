package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

public interface HistoriaService {

    List<HistoriaMedicaRs> getAllHistorias();

    MiRespuestaRS guardarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;

    MiRespuestaRS actualizarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException;
}
