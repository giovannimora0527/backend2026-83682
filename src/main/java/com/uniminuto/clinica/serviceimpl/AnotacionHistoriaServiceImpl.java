package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * Crea una nueva anotación de historia médica.
     *
     * @param anotacion información de la nueva anotación.
     * @return anotación almacenada.
     */
    @Override
    public AnotacionHistoria crearAnotacion(
            AnotacionHistoria anotacion) {

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

        return anotacionHistoriaRepository
                .findByFechaBetweenOrderByFechaDesc(
                        fechaInicial,
                        fechaFinal
                );
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
}