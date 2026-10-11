
package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.dto.MascotaDetalleDTO;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uniminuto.clinica.dto.CrearMascotaDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * API encargada de exponer los endpoints
 * relacionados con las mascotas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/mascota")
public interface MascotaApi {

    /**
     * Consulta todas las mascotas.
     * Conservamos este endpoint para mantener
     * compatibilidad con el código existente.
     */
    @GetMapping(
            value = "/listar-mascotas",
            produces = {"application/json"}
    )
    ResponseEntity<List<Mascota>> listarMascotas()
            throws BadRequestException;

    /**
     * Consulta las mascotas ordenadas
     * alfabéticamente.
     */
    @GetMapping(
            value = "/listar-mascotas-ordenado",
            produces = {"application/json"}
    )
    ResponseEntity<List<Mascota>> listarMascotas(
            @RequestParam boolean ascendente)
            throws BadRequestException;

    /**
     * NUEVO ENDPOINT
     *
     * Devuelve las mascotas con:
     * - Nombre
     * - Edad
     * - Raza
     * - Especie
     * - Propietario
     *
     * Utiliza un DTO en lugar de devolver
     * directamente la entidad de MySQL.
     */
    @GetMapping(
            value = "/listar-detalle",
            produces = {"application/json"}
    )
    ResponseEntity<List<MascotaDetalleDTO>> listarDetalle()
            throws BadRequestException;


    /**
     * ENDPOINT PARA CREAR MASCOTAS
     *
     * Ruta: POST /mascota/crear
     *
     * Recibe los datos enviados desde Angular
     * y devuelve la mascota registrada.
     */
    @PostMapping(
            value = "/crear",
            consumes = "application/json",
            produces = "application/json"
    )
    ResponseEntity<Mascota> crearMascota(
            @RequestBody CrearMascotaDTO datos
    );



}
