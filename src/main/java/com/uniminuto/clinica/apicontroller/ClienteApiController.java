
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClienteApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.service.ClienteService;
import com.uniminuto.clinica.service.MascotaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST encargado de gestionar las consultas de clientes
 * y las mascotas asociadas a cada cliente.
 */
@RestController
public class ClienteApiController implements ClienteApi {

    /**
     * Servicio que permite consultar y gestionar los clientes.
     */
    @Autowired
    private ClienteService clienteService;

    /**
     * Servicio que permite consultar las mascotas registradas.
     */
    @Autowired
    private MascotaService mascotaService;

    /**
     * Obtiene la lista de clientes registrados.
     *
     * @return respuesta HTTP con la lista de clientes
     * @throws BadRequestException si ocurre un error de solicitud al consultar los clientes
     */
    @Override
    public ResponseEntity<List<Cliente>> listarClientes() throws BadRequestException {
        return ResponseEntity.ok(this.clienteService.obtenerClientes());
    }

    /**
     * Consulta un cliente mediante su identificador.
     *
     * @param id identificador del cliente que se desea consultar
     * @return respuesta HTTP con los datos del cliente
     */
    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public ResponseEntity<Cliente> consultarCliente(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerCliente(id));
    }

    /**
     * Obtiene las mascotas asociadas a un cliente.
     * Primero verifica que el cliente exista y después consulta sus mascotas.
     *
     * @param id identificador del cliente
     * @return respuesta HTTP con la lista de mascotas del cliente
     * @throws BadRequestException si ocurre un error de solicitud al validar el cliente
     */
    @org.springframework.web.bind.annotation.GetMapping("/{id}/mascotas")
    public ResponseEntity<List<com.uniminuto.clinica.entity.Mascota>> listarMascotasCliente(
            @org.springframework.web.bind.annotation.PathVariable Long id) throws BadRequestException {
        clienteService.obtenerCliente(id);
        return ResponseEntity.ok(mascotaService.listarMascotasPorCliente(id));
    }
}
