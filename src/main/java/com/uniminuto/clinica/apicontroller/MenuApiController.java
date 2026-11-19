package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MenuApi;
import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.service.IdentidadService;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import com.uniminuto.clinica.service.MenuService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MenuApiController implements MenuApi {
    private final MenuService menuService;

    public MenuApiController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Override
    public ResponseEntity<List<Menu>> listarMenusActivos() {
        return ResponseEntity.ok(menuService.listarMenusActivos());
    }

    @Override
    public ResponseEntity<List<Menu>> obtenerMenusPropios(
            @RequestAttribute(IdentidadService.ATRIBUTO) Identidad identidad) {
        var menus = menuService.obtenerMenusPorUsuario(identidad.usuarioId());
        if (identidad.rol() == Rol.DESPACHO_MEDICO) {
            menus = menus.stream().filter(menu -> "/inicio/medicamentos".equals(menu.getUrl())
                    || "/inicio/despacho".equals(menu.getUrl())).toList();
        } else {
            menus = menus.stream().filter(menu -> !"/inicio/despacho".equals(menu.getUrl())).toList();
        }
        if (identidad.rol() != Rol.ADMIN && identidad.rol() != Rol.RECEPCION) {
            menus = menus.stream().filter(menu -> !"/inicio/facturacion".equals(menu.getUrl())
                    && !"/inicio/reportes".equals(menu.getUrl())).toList();
        }
        return ResponseEntity.ok(menus);
    }

    @Override
    public ResponseEntity<List<Menu>> obtenerMenusPorUsuario(Long usuarioId,
            @RequestAttribute(IdentidadService.ATRIBUTO) Identidad identidad) {
        if (!identidad.usuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes consultar tus propios menús.");
        }
        return obtenerMenusPropios(identidad);
    }
}
