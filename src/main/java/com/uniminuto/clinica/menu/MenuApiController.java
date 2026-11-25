package com.uniminuto.clinica.menu;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Controlador REST del menú: implementa {@link MenuApi} y delega en {@link MenuService}. */
@RestController
public class MenuApiController implements MenuApi {

    /** Menú según el rol. */
    private final MenuService servicio;

    /**
     * @param servicio servicio del menú.
     */
    public MenuApiController(MenuService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MenuDto>> propio() {
        return ResponseEntity.ok(servicio.propio());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MenuDto>> activos() {
        return ResponseEntity.ok(servicio.activos());
    }
}
