package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.MascotaResponse;

import java.util.List;

/**
 * Casos de uso de consulta de mascotas.
 */
public interface MascotaService {

    /**
     * Lista las mascotas activas de un cliente (alimenta el combo dependiente Cliente → Mascota).
     *
     * @param clienteId identificador del cliente
     * @return mascotas del cliente
     */
    List<MascotaResponse> listarPorCliente(Long clienteId);

    /**
     * Consulta una mascota por su identificador.
     *
     * @param id identificador de la mascota
     * @return la mascota
     */
    MascotaResponse obtener(Long id);
}
