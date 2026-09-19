package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/mascota")
public interface MascotaApi {

    @GetMapping(value = "/listar-mascotas",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotas()
            throws BadRequestException;


    @GetMapping(value = "/listar-mascotas-ordenado",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotas(
            @RequestParam boolean ascendente)
            throws BadRequestException;

    @GetMapping(value = "/buscar-by-raza",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Mascota>> buscarPorRaza(
            @RequestParam Integer razaId)
            throws BadRequestException;

    @GetMapping(value = "/buscar-by-cliente",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Mascota>> buscarPorCliente(
            @RequestParam Long clienteId)
            throws BadRequestException;


    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;

}
