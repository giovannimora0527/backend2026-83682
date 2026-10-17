package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/anotacion-historia")
public interface AnotacionHistoriaApi {

    /**
     * Metodo crear historia medica con anotaciones.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearAnotacionHistoria(
            @RequestBody AnotacionHistoriaRq anotacionHistoria
    )
            throws BadRequestException;



    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarAnotaciones() throws BadRequestException;

    @GetMapping(value = "/listar-por-historia", produces = {"application/json"})
    ResponseEntity<List<AnotacionHistoria>> listarPorHistoria(@RequestParam Long historiaId) throws BadRequestException;
}
