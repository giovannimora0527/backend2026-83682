
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MascotaApi;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MascotaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone las operaciones de consulta,
 * registro y actualización de mascotas.
 */
@RestController
public class MascotaApiController implements MascotaApi {

    /** Servicio encargado de gestionar las mascotas. */
    @Autowired
    private MascotaService mascotaService;

    /**
     * Obtiene la lista de mascotas registradas.
     *
     * @return respuesta HTTP con la lista de mascotas
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    @Override
    public ResponseEntity<List<Mascota>> listarMascotas() throws BadRequestException {
        return ResponseEntity.ok(this.mascotaService.listarMascotas());
    }

    /**
     * Obtiene las mascotas ordenadas según el sentido indicado.
     *
     * @param ascendente indica si el orden debe ser ascendente
     * @return respuesta HTTP con la lista ordenada de mascotas
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    @Override
    public ResponseEntity<List<Mascota>> listarMascotas(boolean ascendente)
            throws BadRequestException {
        return ResponseEntity.ok(
                this.mascotaService.listarMascotasOrdenado(ascendente));
    }

    /**
     * Busca las mascotas que pertenecen a una raza determinada.
     *
     * @param razaId identificador de la raza
     * @return respuesta HTTP con las mascotas encontradas
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    @Override
    public ResponseEntity<List<Mascota>> buscarPorRaza(Integer razaId)
            throws BadRequestException {
        return ResponseEntity.ok(
                this.mascotaService.listarMascotasPorRaza(razaId));
    }

    /**
     * Busca las mascotas asociadas a un cliente.
     *
     * @param clienteId identificador del cliente
     * @return respuesta HTTP con las mascotas del cliente
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    @Override
    public ResponseEntity<List<Mascota>> buscarPorCliente(Long clienteId)
            throws BadRequestException {
        return ResponseEntity.ok(
                this.mascotaService.listarMascotasPorCliente(clienteId));
    }

    /**
     * Registra una nueva mascota.
     *
     * @param mascotaRq datos de la mascota que se va a registrar
     * @return respuesta HTTP con estado 201 y el resultado del registro
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarMascota(MascotaRq mascotaRq)
            throws BadRequestException {
        return ResponseEntity.status(201)
                .body(this.mascotaService.guardarMascota(mascotaRq));
    }

    /**
     * Actualiza los datos de una mascota existente.
     *
     * @param mascotaRq datos de la mascota que se va a actualizar
     * @return respuesta HTTP con el resultado de la actualización
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMascota(MascotaRq mascotaRq)
            throws BadRequestException {
        return ResponseEntity.ok(
                this.mascotaService.actualizarMascota(mascotaRq));
    }

    /**
     * Consulta una mascota por su identificador.
     *
     * @param id identificador de la mascota
     * @return respuesta HTTP con los datos de la mascota consultada
     */
    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public ResponseEntity<Mascota> consultarMascota(
            @org.springframework.web.bind.annotation.PathVariable Integer id) {
        return ResponseEntity.ok(mascotaService.consultarMascota(id));
    }
}
