package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * API para la gestión de mascotas.
 */
@Tag(name = "Mascotas")
@RequestMapping("/mascota")
public interface MascotaApi {

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Listar mascotas")
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotas()
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Listar mascotas ordenadas por nombre")
    @GetMapping(value = "/listar-ordenado",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotasOrdenado(
            @RequestParam boolean ascendente)
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Buscar mascotas por nombre")
    @GetMapping(value = "/buscar-nombre",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> buscarMascotaPorNombre(
            @RequestParam String nombre)
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Mascotas de un cliente")
    @GetMapping(value = "/buscar-by-cliente",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> buscarMascotaPorCliente(
            @RequestParam Long clienteId)
            throws BadRequestException;

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Mascotas de una raza")
    @GetMapping(value = "/buscar-by-raza",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> buscarMascotaPorRaza(
            @RequestParam Integer razaId)
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Crear una mascota")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Actualizar una mascota")
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;
}
