package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.apache.coyote.BadRequestException;
import com.uniminuto.clinica.service.AlcanceMedico;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio encargado de gestionar
 * las anotaciones de las historias médicas.
 */
@Service
public class AnotacionHistoriaServiceImpl
        implements AnotacionHistoriaService {

    /**
     * Repositorio utilizado para consultar y guardar
     * las anotaciones en la base de datos.
     */
    private final AnotacionHistoriaRepository anotacionHistoriaRepository;
    private final AlcanceMedico alcance;

    public AnotacionHistoriaServiceImpl(AnotacionHistoriaRepository repositorio, AlcanceMedico alcance) {
        this.anotacionHistoriaRepository = repositorio;
        this.alcance = alcance;
    }

    /**
     * Crea una nueva anotación de historia médica.
     *
     * @param anotacion información de la nueva anotación.
     * @return anotación almacenada.
     */
    @Override
    @Transactional
    public AnotacionHistoria crearAnotacion(
            AnotacionHistoria anotacion) {

        alcance.exigirMedico(anotacion.getMedicoId());
        alcance.exigirHistoria(anotacion.getHistoriaId());
        anotacion.setId(null);

        return anotacionHistoriaRepository.save(anotacion);
    }

    /**
     * Busca las anotaciones entre una fecha inicial
     * y una fecha final.
     *
     * @param fechaInicial fecha inicial de búsqueda.
     * @param fechaFinal fecha final de búsqueda.
     * @return lista de anotaciones encontradas.
     */
    @Override
    public List<AnotacionHistoria> buscarAnotacionesPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal) {

        return filtrarAnotaciones(anotacionHistoriaRepository
                .findByFechaBetweenOrderByFechaDesc(
                        fechaInicial,
                        fechaFinal
                ));
    }

    /**
     * Actualiza una anotación existente.
     *
     * @param id identificador de la anotación.
     * @param anotacion información actualizada.
     * @return anotación actualizada.
     * @throws BadRequestException si la anotación no existe.
     */
    @Override
    @Transactional
    public AnotacionHistoria actualizarAnotacion(
            Long id,
            AnotacionHistoria anotacion)
            throws BadRequestException {

        AnotacionHistoria anotacionExistente =
                anotacionHistoriaRepository.findById(id)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "No existe una anotacion con el id: " + id
                                )
                        );

        alcance.exigirMedico(anotacionExistente.getMedicoId());
        alcance.exigirHistoria(anotacionExistente.getHistoriaId());
        alcance.exigirMedico(anotacion.getMedicoId());
        alcance.exigirHistoria(anotacion.getHistoriaId());
        anotacionExistente.setHistoriaId(
                anotacion.getHistoriaId()
        );

        anotacionExistente.setMedicoId(
                anotacion.getMedicoId()
        );

        anotacionExistente.setFecha(
                anotacion.getFecha()
        );

        anotacionExistente.setDescripcion(
                anotacion.getDescripcion()
        );

        return anotacionHistoriaRepository
                .save(anotacionExistente);
    }


    // =====================================================
    // LISTAR TODAS LAS ANOTACIONES
    // =====================================================

    /**
     * Obtiene todas las anotaciones guardadas
     * en la tabla anotacion_historia.
     */
    @Override
    public List<AnotacionHistoria> listarTodasLasAnotaciones() {

        return filtrarAnotaciones(anotacionHistoriaRepository.findAll());

    }

    private List<AnotacionHistoria> filtrarAnotaciones(List<AnotacionHistoria> anotaciones) {
        Integer medicoId = alcance.medicoId();
        if (medicoId == null) {
            return anotaciones;
        }
        var historias = alcance.historias(medicoId);
        return anotaciones.stream().filter(anotacion -> medicoId.equals(anotacion.getMedicoId())
                && anotacion.getHistoriaId() != null
                && historias.contains(anotacion.getHistoriaId().longValue())).toList();
    }
}
