package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.Cita;
import org.apache.coyote.BadRequestException ;
import java.util.List;
import java.time.LocalDateTime;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

public interface CitaService {
    List<Cita>listarCita (LocalDateTime fechaInicial, LocalDateTime fechaFinal)
            throws  BadRequestException ;

    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;
}
