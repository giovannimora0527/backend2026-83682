package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.ValidadorFechas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/** Implementacion de {@link CitaService}. */
@Service
public class CitaServiceImpl implements CitaService {

    /** Repositorio de citas. */
    @Autowired
    private CitaRepository citaRepository;

    /** {@inheritDoc} */
    @Override
    public CitaRs crearCita(CitaRq citaRq) {
        validarDatos(citaRq);
        Cita cita = new Cita();
        cita.setFechaCita(citaRq.getFechaCita());
        cita.setMotivo(citaRq.getMotivo());
        return aRespuesta(citaRepository.save(cita));
    }

    /** {@inheritDoc} Busca la cita y modifica sus campos para que sea un UPDATE. */
    @Override
    public CitaRs actualizarCita(CitaRq citaRq) {
        validarDatos(citaRq);
        if (citaRq.getCitaId() == null) {
            throw new BadRequestException("El citaId es obligatorio para actualizar");
        }
        Cita cita = citaRepository.findById(citaRq.getCitaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una cita con el id " + citaRq.getCitaId()));
        cita.setFechaCita(citaRq.getFechaCita());
        cita.setMotivo(citaRq.getMotivo());
        return aRespuesta(citaRepository.save(cita));
    }

    /** {@inheritDoc} */
    @Override
    public List<CitaRs> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        ValidadorFechas.validarRango(fechaInicial, fechaFinal);
        return citaRepository.findByFechaCitaBetweenOrderByFechaCitaDesc(fechaInicial, fechaFinal)
                .stream().map(this::aRespuesta).toList();
    }

    /** Valida los campos obligatorios comunes a crear y actualizar. */
    private void validarDatos(CitaRq citaRq) {
        if (citaRq == null) {
            throw new BadRequestException("El cuerpo de la peticion es obligatorio");
        }
        if (citaRq.getFechaCita() == null) {
            throw new BadRequestException("La fechaCita es obligatoria");
        }
    }

    /** Convierte la entidad en su DTO de respuesta. */
    private CitaRs aRespuesta(Cita cita) {
        CitaRs r = new CitaRs();
        r.setCitaId(cita.getCitaId());
        r.setFechaCita(cita.getFechaCita());
        r.setMotivo(cita.getMotivo());
        return r;
    }
}
