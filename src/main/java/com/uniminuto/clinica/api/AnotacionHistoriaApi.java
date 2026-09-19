package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API que define los servicios relacionados
 * con las anotaciones de las historias médicas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Crea una nueva anotación de historia médica.
     *
     * @param anotacion información de la anotación.
     * @return respuesta con la anotación creada.
     */
    @PostMapping(
            value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<AnotacionHistoria> crearAnotacion(
            @RequestBody AnotacionHistoria anotacion
    );

    /**
     * Busca las anotaciones entre una fecha inicial
     * y una fecha final.
     *
     * @param fechaInicial fecha inicial de búsqueda.
     * @param fechaFinal fecha final de búsqueda.
     * @return lista de anotaciones encontradas.
     */
    @GetMapping(
            value = "/filtrar-fechas",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<List<AnotacionHistoria>> filtrarAnotacionesPorFechas(

            @RequestParam("fechaInicial")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaInicial,

            @RequestParam("fechaFinal")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaFinal
    );

    /**
     * Actualiza una anotación existente.
     *
     * @param id identificador de la anotación.
     * @param anotacion información actualizada.
     * @return respuesta con la anotación actualizada.
     * @throws BadRequestException si la anotación no existe.
     */
    @PutMapping(
            value = "/actualizar/{id}",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<AnotacionHistoria> actualizarAnotacion(
            @PathVariable("id") Long id,
            @RequestBody AnotacionHistoria anotacion
    ) throws BadRequestException;
}