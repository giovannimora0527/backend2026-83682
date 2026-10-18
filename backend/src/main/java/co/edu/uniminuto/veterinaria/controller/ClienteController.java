package co.edu.uniminuto.veterinaria.controller;

import co.edu.uniminuto.veterinaria.dto.ClienteResponse;
import co.edu.uniminuto.veterinaria.dto.MascotaResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;
import co.edu.uniminuto.veterinaria.service.ClienteService;
import co.edu.uniminuto.veterinaria.service.MascotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de clientes: GET /api/clientes, GET /api/clientes/{id}, GET /api/clientes/{id}/mascotas.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    /** Servicio de clientes. */
    private final ClienteService clienteService;

    /** Servicio de mascotas. */
    private final MascotaService mascotaService;

    /**
     * Crea el controlador.
     *
     * @param clienteService servicio de clientes
     * @param mascotaService servicio de mascotas
     */
    public ClienteController(ClienteService clienteService, MascotaService mascotaService) {
        this.clienteService = clienteService;
        this.mascotaService = mascotaService;
    }

    /**
     * Lista clientes activos con búsqueda y paginación.
     *
     * @param texto  texto a buscar (opcional)
     * @param pagina número de página (base 0)
     * @param tamano registros por página
     * @return página de clientes
     */
    @GetMapping
    public PageResponse<ClienteResponse> listar(@RequestParam(required = false) String texto,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "100") int tamano) {
        return clienteService.listar(texto, pagina, tamano);
    }

    /**
     * Consulta un cliente.
     *
     * @param id identificador del cliente
     * @return el cliente
     */
    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    /**
     * Lista las mascotas de un cliente (combo dependiente Cliente → Mascota).
     *
     * @param id identificador del cliente
     * @return mascotas activas del cliente
     */
    @GetMapping("/{id}/mascotas")
    public List<MascotaResponse> listarMascotas(@PathVariable Long id) {
        return mascotaService.listarPorCliente(id);
    }
}
