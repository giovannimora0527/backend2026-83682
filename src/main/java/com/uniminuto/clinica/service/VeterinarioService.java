
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Veterinario;
import java.util.List;

/**
 * Define las operaciones de consulta de veterinarios.
 */
public interface VeterinarioService {

    /**
     * Obtiene todos los veterinarios registrados.
     *
     * @return lista de veterinarios registrados
     */
    List<Veterinario> listar();

    /**
     * Consulta un veterinario por su identificador.
     *
     * @param id identificador del veterinario
     * @return veterinario encontrado
     */
    Veterinario consultar(Long id);
}
