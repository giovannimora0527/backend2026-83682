package co.edu.uniminuto.veterinaria.service;

import co.edu.uniminuto.veterinaria.dto.VeterinarioResponse;

import java.util.List;

/**
 * Casos de uso de consulta de veterinarios.
 */
public interface VeterinarioService {

    /**
     * Lista los veterinarios activos.
     *
     * @return veterinarios activos ordenados por nombre
     */
    List<VeterinarioResponse> listar();
}
