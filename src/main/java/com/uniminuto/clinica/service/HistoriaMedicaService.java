package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.apache.coyote.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoriaMedicaService {
    List<HistoriaMedica> listarHistorias(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    ) throws BadRequestException;
    MiRespuestaRS guardarHistoriaMedica(
            HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException;

    MiRespuestaRS actualizarHistoriaMedica(
            HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException;

}