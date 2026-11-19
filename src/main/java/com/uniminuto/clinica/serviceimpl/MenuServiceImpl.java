
package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.repository.MenuRepository;
import com.uniminuto.clinica.service.MenuService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Implementación de la gestión de menús.
 *
 * Consulta la tabla menu y las relaciones
 * usuario -> perfil -> perfil_menu -> menu.
 */
@Service
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    // Inyectar el repositorio de menús.
    public MenuServiceImpl(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    /**
     * Obtener todos los menús activos.
     */
    @Override
    public List<Menu> listarMenusActivos() {

        return menuRepository
                .findByActivoTrueOrderByMenuIdAsc();

    }

    /**
     * Obtener únicamente los menús que
     * corresponden al perfil del usuario.
     */
    @Override
    public List<Menu> obtenerMenusPorUsuario(Long usuarioId) {

        // Verificar que el ID recibido sea válido.
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del usuario no es válido."
            );
        }

        // Consultar los permisos definidos en MySQL.
        return menuRepository
                .obtenerMenusPorUsuario(usuarioId);

    }

}
