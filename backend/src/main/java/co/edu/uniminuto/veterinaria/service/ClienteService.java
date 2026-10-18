package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.ClienteResponse;
import co.edu.uniminuto.veterinaria.dto.PageResponse;

/**
 * Casos de uso de consulta de clientes.
 */
public interface ClienteService {

    /**
     * Lista los clientes activos, con búsqueda opcional por nombre o documento.
     *
     * @param texto  texto a buscar en nombres, apellidos o documento (opcional)
     * @param pagina número de página (base 0)
     * @param tamano registros por página
     * @return página de clientes
     */
    PageResponse<ClienteResponse> listar(String texto, int pagina, int tamano);

    /**
     * Consulta un cliente por su identificador.
     *
     * @param id identificador del cliente
     * @return el cliente
     */
    ClienteResponse obtener(Long id);
}
