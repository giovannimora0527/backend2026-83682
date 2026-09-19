package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import org.apache.coyote.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de definir las operaciones
 * relacionadas con las citas de la clínica.
 */
public interface CitaService {

    /**
     * Busca las citas registradas entre una fecha inicial
     * y una fecha final, ordenadas desde la más reciente
     * hasta la más antigua.
     *
     * @param fechaInicial fecha y hora inicial de búsqueda.
     * @param fechaFinal fecha y hora final de búsqueda.
     * @return lista de citas encontradas.
     */
    List<Cita> buscarCitasPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    );
    /**
     * Guarda una nueva cita en el sistema.
     *
     * @param cita información de la nueva cita.
     * @return cita almacenada.
     */
    Cita crearCita(Cita cita);

    /**
     * Actualiza una cita que ya existe.
     *
     * @param id identificador de la cita que se desea actualizar.
     * @param cita información actualizada de la cita.
     * @return cita actualizada.
     */
    Cita actualizarCita(Long id, Cita cita) throws BadRequestException;
}