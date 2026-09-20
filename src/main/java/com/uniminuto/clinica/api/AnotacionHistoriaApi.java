package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/** Contrato REST del CRUD de historia medica / anotaciones (requerimiento 5). */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Crea una anotacion en una historia medica existente.
     *
     * @param anotacionRq datos de la anotacion.
     * @return la anotacion creada.
     */
    @PostMapping(value = "/guardar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<AnotacionHistoriaRs> guardarAnotacion(@RequestBody AnotacionHistoriaRq anotacionRq);

    /**
     * Actualiza una anotacion existente.
     *
     * @param anotacionRq datos de la anotacion, incluyendo su id.
     * @return la anotacion actualizada.
     */
    @PostMapping(value = "/actualizar", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<AnotacionHistoriaRs> actualizarAnotacion(@RequestBody AnotacionHistoriaRq anotacionRq);

    /**
     * Lista todas las anotaciones.
     *
     * @return anotaciones ordenadas de la mas reciente a la mas antigua.
     */
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoriaRs>> listarAnotaciones();

    /**
     * Filtra anotaciones por rango de fechas.
     *
     * @param fechaInicial limite inferior (formato yyyy-MM-ddTHH:mm:ss).
     * @param fechaFinal   limite superior (formato yyyy-MM-ddTHH:mm:ss).
     * @return anotaciones del rango, de la mas reciente a la mas antigua.
     */
    @GetMapping(value = "/filtrar", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoriaRs>> filtrarAnotaciones(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFinal);
}
