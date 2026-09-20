package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import com.uniminuto.clinica.util.ValidadorFechas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/** Implementacion de {@link AnotacionHistoriaService}. */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /** Repositorio de anotaciones. */
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /** Repositorio de historias medicas, para validar la referencia. */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /** {@inheritDoc} */
    @Override
    public AnotacionHistoriaRs crearAnotacion(AnotacionHistoriaRq anotacionRq) {
        validarDatos(anotacionRq);
        if (anotacionRq.getHistoriaMedicaId() == null) {
            throw new BadRequestException("El historiaMedicaId es obligatorio para crear");
        }
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoriaMedica(buscarHistoria(anotacionRq.getHistoriaMedicaId()));
        anotacion.setFechaAnotacion(anotacionRq.getFechaAnotacion());
        anotacion.setDetalle(anotacionRq.getDetalle());
        return aRespuesta(anotacionHistoriaRepository.save(anotacion));
    }

    /** {@inheritDoc} Si llega un nuevo historiaMedicaId tambien reasigna la relacion. */
    @Override
    public AnotacionHistoriaRs actualizarAnotacion(AnotacionHistoriaRq anotacionRq) {
        validarDatos(anotacionRq);
        if (anotacionRq.getAnotacionHistoriaId() == null) {
            throw new BadRequestException("El anotacionHistoriaId es obligatorio para actualizar");
        }
        AnotacionHistoria anotacion = anotacionHistoriaRepository
                .findById(anotacionRq.getAnotacionHistoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una anotacion con el id " + anotacionRq.getAnotacionHistoriaId()));
        if (anotacionRq.getHistoriaMedicaId() != null) {
            anotacion.setHistoriaMedica(buscarHistoria(anotacionRq.getHistoriaMedicaId()));
        }
        anotacion.setFechaAnotacion(anotacionRq.getFechaAnotacion());
        anotacion.setDetalle(anotacionRq.getDetalle());
        return aRespuesta(anotacionHistoriaRepository.save(anotacion));
    }

    /** {@inheritDoc} */
    @Override
    public List<AnotacionHistoriaRs> listarAnotaciones() {
        return anotacionHistoriaRepository.findAllByOrderByFechaAnotacionDesc()
                .stream().map(this::aRespuesta).toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<AnotacionHistoriaRs> filtrarAnotaciones(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        ValidadorFechas.validarRango(fechaInicial, fechaFinal);
        return anotacionHistoriaRepository
                .findByFechaAnotacionBetweenOrderByFechaAnotacionDesc(fechaInicial, fechaFinal)
                .stream().map(this::aRespuesta).toList();
    }

    /** Busca la historia medica o lanza {@link RecursoNoEncontradoException}. */
    private HistoriaMedica buscarHistoria(Long historiaMedicaId) {
        return historiaMedicaRepository.findById(historiaMedicaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una historia medica con el id " + historiaMedicaId));
    }

    /** Valida los campos obligatorios comunes a crear y actualizar. */
    private void validarDatos(AnotacionHistoriaRq anotacionRq) {
        if (anotacionRq == null) {
            throw new BadRequestException("El cuerpo de la peticion es obligatorio");
        }
        if (anotacionRq.getFechaAnotacion() == null) {
            throw new BadRequestException("La fechaAnotacion es obligatoria");
        }
        if (anotacionRq.getDetalle() == null || anotacionRq.getDetalle().isBlank()) {
            throw new BadRequestException("El detalle es obligatorio");
        }
    }

    /** Convierte la entidad en su DTO de respuesta, aplanando la historia asociada. */
    private AnotacionHistoriaRs aRespuesta(AnotacionHistoria anotacion) {
        AnotacionHistoriaRs r = new AnotacionHistoriaRs();
        r.setAnotacionHistoriaId(anotacion.getAnotacionHistoriaId());
        r.setFechaAnotacion(anotacion.getFechaAnotacion());
        r.setDetalle(anotacion.getDetalle());
        HistoriaMedica historia = anotacion.getHistoriaMedica();
        if (historia != null) {
            r.setHistoriaMedicaId(historia.getHistoriaMedicaId());
            r.setDescripcionGeneralHistoria(historia.getDescripcionGeneral());
        }
        return r;
    }
}
