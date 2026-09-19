package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador encargado de atender las peticiones
 * relacionadas con las anotaciones de historias médicas.
 */
@RestController
public class AnotacionHistoriaApiController
        implements AnotacionHistoriaApi {

    /**
     * Servicio utilizado para gestionar
     * las anotaciones de historia médica.
     */
    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    /**
     * Crea una nueva anotación.
     *
     * @param anotacion información de la anotación.
     * @return respuesta con la anotación creada.
     */
    @Override
    public ResponseEntity<AnotacionHistoria> crearAnotacion(
            AnotacionHistoria anotacion) {

        return ResponseEntity.ok(
                this.anotacionHistoriaService.crearAnotacion(anotacion)
        );
    }

    /**
     * Filtra las anotaciones entre dos fechas.
     *
     * @param fechaInicial fecha inicial.
     * @param fechaFinal fecha final.
     * @return lista de anotaciones encontradas.
     */
    @Override
    public ResponseEntity<List<AnotacionHistoria>>
    filtrarAnotacionesPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal) {

        return ResponseEntity.ok(
                this.anotacionHistoriaService
                        .buscarAnotacionesPorFechas(
                                fechaInicial,
                                fechaFinal

                        )
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
    public ResponseEntity<AnotacionHistoria> actualizarAnotacion(
            Long id,
            AnotacionHistoria anotacion)
            throws BadRequestException {

        return ResponseEntity.ok(
                this.anotacionHistoriaService
                        .actualizarAnotacion(id, anotacion)
        );
    }
}