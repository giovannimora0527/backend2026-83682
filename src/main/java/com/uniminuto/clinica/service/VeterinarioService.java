
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

    /**
     * Busca un médico por su documento (servicio del parcial).
     *
     * @param tipoDocumento tipo de documento
     * @param numeroDocumento número de documento
     * @return médico encontrado
     */
    Veterinario buscarPorDocumento(String tipoDocumento, String numeroDocumento);

    /**
     * Busca un médico por su registro profesional (servicio del parcial).
     *
     * @param registroProfesional registro profesional
     * @return médico encontrado
     */
    Veterinario buscarPorRegistroProfesional(String registroProfesional);

    /**
     * Registra un médico nuevo.
     *
     * @param request datos del médico
     * @return médico creado
     */
    Veterinario crear(com.uniminuto.clinica.models.VeterinarioRequest request);

    /**
     * Actualiza los datos de un médico existente.
     *
     * @param id identificador del médico
     * @param request nuevos datos del médico
     * @return médico actualizado
     */
    Veterinario actualizar(Long id, com.uniminuto.clinica.models.VeterinarioRequest request);
}
