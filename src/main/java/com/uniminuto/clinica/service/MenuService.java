
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Menu;
import java.util.List;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Servicio para consultar los menús del sistema.
 */
public interface MenuService {

    /**
     * Consultar todos los menús activos.
     */
    List<Menu> listarMenusActivos();

    /**
     * Consultar los menús autorizados
     * para un usuario según su perfil.
     */
    List<Menu> obtenerMenusPorUsuario(Long usuarioId);

}
