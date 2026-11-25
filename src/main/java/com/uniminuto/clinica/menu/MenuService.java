package com.uniminuto.clinica.menu;

import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.stereotype.Service;

import java.util.List;

/** Menú lateral según el rol del usuario autenticado (restricción visual; el backend valida los permisos). */
@Service
public class MenuService {

    /** Acceso a datos del menú. */
    private final MenuRepository repositorio;

    /**
     * @param repositorio repositorio del menú.
     */
    public MenuService(MenuRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** @return opciones del rol del usuario autenticado. */
    public List<MenuDto> propio() {
        return repositorio.delPerfil(SesionActual.obtener().perfilId());
    }

    /** @return todas las opciones activas. */
    public List<MenuDto> activos() {
        return repositorio.activos();
    }
}
