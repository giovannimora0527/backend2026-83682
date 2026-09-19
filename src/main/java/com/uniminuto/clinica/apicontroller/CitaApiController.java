package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.service.CitaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador encargado de atender las peticiones
 * relacionadas con las citas de la clínica.
 */
@RestController
public class CitaApiController implements CitaApi {

    /**
     * Servicio utilizado para gestionar
     * las operaciones relacionadas con las citas.
     */
    @Autowired
    private CitaService citaService;

    /**
     * Filtra las citas entre una fecha inicial
     * y una fecha final.
     *
     * @param fechaInicial fecha y hora inicial de búsqueda.
     * @param fechaFinal fecha y hora final de búsqueda.
     * @return respuesta HTTP con las citas encontradas.
     * @throws BadRequestException si ocurre un error en la solicitud.
     */
    @Override
    public ResponseEntity<List<Cita>> filtrarCitasPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal)
            throws BadRequestException {

        return ResponseEntity.ok(
                this.citaService.buscarCitasPorFechas(
                        fechaInicial,
                        fechaFinal
                )
        );
    }
    /**
     * Crea una nueva cita.
     *
     * @param cita información de la nueva cita.
     * @return respuesta HTTP con la cita creada.
     */
    @Override
    public ResponseEntity<Cita> crearCita(Cita cita) {

        return ResponseEntity.ok(
                this.citaService.crearCita(cita)
        );
    }

    /**
     * Actualiza una cita existente.
     *
     * @param id identificador de la cita.
     * @param cita información actualizada.
     * @return respuesta HTTP con la cita actualizada.
     * @throws BadRequestException si la cita no existe.
     */
    @Override
    public ResponseEntity<Cita> actualizarCita(
            Long id,
            Cita cita)
            throws BadRequestException {

        return ResponseEntity.ok(
                this.citaService.actualizarCita(id, cita)
        );
    }
}