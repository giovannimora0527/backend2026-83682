package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la lógica de negocio de historias médicas y anotaciones.
 * Implementa las operaciones de consulta, creación y actualización según los
 * requerimientos del parcial.
 *
 * @author ANRUIZSS
 * @version 1.0
 */
@Service
public class HistoriaMedicaService {

    /**
     * Repositorio para acceder a datos de historias médicas.
     */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Repositorio para acceder a datos de anotaciones de historia médica.
     */
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * REQUERIMIENTO 5: Crea una nueva historia médica en el sistema.
     *
     * @param historiaMedica La historia médica a crear con pacienteId y fechaCreacion.
     * @return La historia médica creada con su identificador asignado.
     */
    public HistoriaMedica crear(HistoriaMedica historiaMedica) {
        return historiaMedicaRepository.save(historiaMedica);
    }

    /**
     * REQUERIMIENTO 5: Lista historias médicas dentro de un rango de fechas,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio Fecha inicial del rango de filtrado (inclusive).
     * @param fechaFin Fecha final del rango de filtrado (inclusive).
     * @return Lista de historias médicas dentro del rango de fechas especificado.
     */
    public List<HistoriaMedica> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return historiaMedicaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicio, fechaFin);
    }

    /**
     * REQUERIMIENTO 5: Crea una nueva anotación para una historia médica.
     *
     * @param anotacion La anotación a crear con historiaId, medicoId, fecha y descripcion.
     * @return La anotación creada con su identificador asignado.
     */
    public AnotacionHistoria crearAnotacion(AnotacionHistoria anotacion) {
        return anotacionHistoriaRepository.save(anotacion);
    }

    /**
     * REQUERIMIENTO 5: Lista todas las anotaciones de una historia médica específica,
     * ordenadas por fecha de la más reciente a la más antigua.
     *
     * @param historiaId Identificador de la historia médica.
     * @return Lista de anotaciones de la historia médica especificada.
     */
    public List<AnotacionHistoria> listarAnotaciones(Integer historiaId) {
        return anotacionHistoriaRepository.findByHistoriaIdOrderByFechaDesc(historiaId);
    }

    /**
     * REQUERIMIENTO 5: Actualiza una anotación existente en el sistema.
     *
     * @param id Identificador de la anotación a actualizar.
     * @param anotacionDetalles Los nuevos datos de la anotación.
     * @return La anotación actualizada, o null si no existe la anotación.
     */
    public AnotacionHistoria actualizarAnotacion(Long id, AnotacionHistoria anotacionDetalles) {
        Optional<AnotacionHistoria> opt = anotacionHistoriaRepository.findById(id);

        if (opt.isPresent()) {
            AnotacionHistoria anotacionExistente = opt.get();
            anotacionExistente.setHistoriaId(anotacionDetalles.getHistoriaId());
            anotacionExistente.setMedicoId(anotacionDetalles.getMedicoId());
            anotacionExistente.setFecha(anotacionDetalles.getFecha());
            anotacionExistente.setDescripcion(anotacionDetalles.getDescripcion());

            return anotacionHistoriaRepository.save(anotacionExistente);
        }

        return null;
    }
}