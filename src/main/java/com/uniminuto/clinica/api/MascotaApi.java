package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/mascota")
public interface MascotaApi {

    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotas()
            throws BadRequestException;

    /** {@code /listar-mascotas-ordenado} lo usa Angular; {@code /listar-ordenado} es la ruta del parcial. */
    @GetMapping(value = {"/listar-mascotas-ordenado", "/listar-ordenado"},
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotas(
            @RequestParam boolean ascendente)
            throws BadRequestException;

    /** Busca una mascota por nombre (servicio del parcial). */
    @GetMapping(value = "/buscar",
            produces = {"application/json"})
    ResponseEntity<Mascota> buscarPorNombre(
            @RequestParam String nombre)
            throws BadRequestException;

    @GetMapping(value = "/buscar-by-raza",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> buscarPorRaza(
            @RequestParam Integer razaId)
            throws BadRequestException;

    @GetMapping(value = "/buscar-by-cliente",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> buscarPorCliente(
            @RequestParam Long clienteId)
            throws BadRequestException;

    @GetMapping(value = "/{id}",
            produces = {"application/json"})
    ResponseEntity<Mascota> consultarMascota(
            @PathVariable Integer id);

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;

    /** Angular usa POST; también se acepta PUT. */
    @RequestMapping(value = "/actualizar",
            method = {RequestMethod.POST, RequestMethod.PUT},
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;

    /** Elimina una mascota sin citas ni historias asociadas; si las tiene responde 409. */
    @DeleteMapping(value = "/eliminar/{id}",
            produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> eliminarMascota(
            @PathVariable Integer id)
            throws BadRequestException;
}
