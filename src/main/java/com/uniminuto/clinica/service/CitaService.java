package com.uniminuto.clinica.service;

import com.uniminuto.clinica.model.Cita;
import com.uniminuto.clinica.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las citas médicas.
 * Implementa la lógica de negocio para operaciones con citas.
 *
 * @author TuNombre
 * @version 1.0
 */
@Service
public class CitaService {

    /**
     * Repositorio para acceder a datos de citas.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Filtra las citas dentro de un rango de fechas específico,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio Fecha inicial del rango de filtrado.
     * @param fechaFin Fecha final del rango de filtrado.
     * @return Lista de citas dentro del rango de fechas especificado.
     */
    public List<Cita> filtrarCitasPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicio, fechaFin);
    }

    /**
     * Obtiene todas las citas del sistema.
     *
     * @return Lista de todas las citas.
     */
    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    /**
     * Busca una cita por su identificador.
     *
     * @param id Identificador de la cita.
     * @return Optional conteniendo la cita si existe.
     */
    public Optional<Cita> buscarPorId(Long id) {
        return citaRepository.findById(id);
    }

    /**
     * Crea una nueva cita en el sistema.
     *
     * @param cita La cita a crear.
     * @return La cita creada con su identificador asignado.
     */
    public Cita crearCita(Cita cita) {
        return citaRepository.save(cita);
    }

    /**
     * Actualiza una cita existente en el sistema.
     *
     * @param id Identificador de la cita a actualizar.
     * @param citaDetalles Los nuevos datos de la cita.
     * @return La cita actualizada, o null si no existe.
     */
    public Cita actualizarCita(Long id, Cita citaDetalles) {
        Optional<Cita> citaOptional = citaRepository.findById(id);

        if (citaOptional.isPresent()) {
            Cita citaExistente = citaOptional.get();
            citaExistente.setClienteId(citaDetalles.getClienteId());
            citaExistente.setMascotaId(citaDetalles.getMascotaId());
            citaExistente.setMedicoId(citaDetalles.getMedicoId());
            citaExistente.setFechaHora(citaDetalles.getFechaHora());
            citaExistente.setEstado(citaDetalles.getEstado());
            citaExistente.setMotivo(citaDetalles.getMotivo());

            return citaRepository.save(citaExistente);
        }

        return null;
    }
}
