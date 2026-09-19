package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API que define los servicios web
 * relacionados con las citas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Busca las citas comprendidas entre una fecha inicial
     * y una fecha final, ordenadas desde la más reciente
     * hasta la más antigua.
     *
     * @param fechaInicial fecha y hora inicial de búsqueda.
     * @param fechaFinal fecha y hora final de búsqueda.
     * @return respuesta con la lista de citas encontradas.
     * @throws BadRequestException si ocurre un error en la solicitud.
     */
    @GetMapping(
            value = "/filtrar-fechas",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<List<Cita>> filtrarCitasPorFechas(

            @RequestParam("fechaInicial")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicial,

            @RequestParam("fechaFinal")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFinal

    ) throws BadRequestException;

    /**
     * Permite crear una nueva cita.
     *
     * @param cita información de la nueva cita.
     * @return respuesta con la cita creada.
     */
    @PostMapping(
            value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<Cita> crearCita(
            @RequestBody Cita cita
    );

    /**
     * Permite actualizar una cita existente.
     *
     * @param id identificador de la cita.
     * @param cita información actualizada.
     * @return respuesta con la cita actualizada.
     * @throws BadRequestException si la cita no existe.
     */
    @PutMapping(
            value = "/actualizar/{id}",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<Cita> actualizarCita(
            @PathVariable("id") Long id,
            @RequestBody Cita cita
    ) throws BadRequestException;
}