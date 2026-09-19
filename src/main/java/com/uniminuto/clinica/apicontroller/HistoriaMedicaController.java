package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestionar historias médicas y anotaciones.
 * Expone endpoints para operaciones CRUD según el requerimiento 5 del parcial.
 *
 * @author ANRUIZSS
 * @version 1.0
 */
@RestController
@RequestMapping("/api/historias-medicas")
@CrossOrigin(origins = "*")
public class HistoriaMedicaController {

    /**
     * Servicio para gestionar historias médicas y anotaciones.
     */
    @Autowired
    private HistoriaMedicaService historiaMedicaService;

    /**
     * REQUERIMIENTO 5: Crea una nueva historia médica en el sistema.
     *
     * @param historiaMedica Objeto JSON con los datos de la historia médica
     *                       (pacienteId y fechaCreacion).
     * @return La historia médica creada con su identificador.
     */
    @PostMapping
    public HistoriaMedica crearHistoriaMedica(@RequestBody HistoriaMedica historiaMedica) {
        return historiaMedicaService.crear(historiaMedica);
    }

    /**
     * REQUERIMIENTO 5: Lista historias médicas dentro de un rango de fechas,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicio Fecha y hora inicial del rango de filtrado
     *                    (formato ISO: yyyy-MM-ddTHH:mm).
     * @param fechaFin Fecha y hora final del rango de filtrado
     *                 (formato ISO: yyyy-MM-ddTHH:mm).
     * @return Lista de historias médicas filtradas y ordenadas por fecha de creación.
     */
    @GetMapping("/filtrar")
    public List<HistoriaMedica> listarHistoriasPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return historiaMedicaService.listarPorFechas(fechaInicio, fechaFin);
    }

    /**
     * REQUERIMIENTO 5: Crea una nueva anotación para una historia médica.
     *
     * @param anotacion Objeto JSON con los datos de la anotación
     *                  (historiaId, medicoId, fecha y descripcion).
     * @return La anotación creada con su identificador.
     */
    @PostMapping("/anotaciones")
    public AnotacionHistoria crearAnotacion(@RequestBody AnotacionHistoria anotacion) {
        return historiaMedicaService.crearAnotacion(anotacion);
    }

    /**
     * REQUERIMIENTO 5: Lista todas las anotaciones de una historia médica específica,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param historiaId Identificador de la historia médica.
     * @return Lista de anotaciones de la historia especificada.
     */
    @GetMapping("/anotaciones/{historiaId}")
    public List<AnotacionHistoria> listarAnotacionesPorHistoria(@PathVariable Integer historiaId) {
        return historiaMedicaService.listarAnotaciones(historiaId);
    }

    /**
     * REQUERIMIENTO 5: Actualiza una anotación existente en el sistema.
     *
     * @param id Identificador de la anotación a actualizar.
     * @param anotacion Objeto JSON con los nuevos datos de la anotación.
     * @return La anotación actualizada.
     */
    @PutMapping("/anotaciones/{id}")
    public AnotacionHistoria actualizarAnotacion(@PathVariable Long id, @RequestBody AnotacionHistoria anotacion) {
        return historiaMedicaService.actualizarAnotacion(id, anotacion);
    }
}