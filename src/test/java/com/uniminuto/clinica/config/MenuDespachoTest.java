package com.uniminuto.clinica.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.uniminuto.clinica.apicontroller.MenuApiController;
import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import com.uniminuto.clinica.service.MenuService;
import org.junit.jupiter.api.Test;

class MenuDespachoTest {
    @Test void conservaInventarioYExigeRolMasPermisoDeBaseDeDatos() {
        MenuService menus=mock(MenuService.class);
        var controlador=new MenuApiController(menus);
        var despacho=new Identidad(1L,"despacho",4,"DESPACHO_MEDICO",Rol.DESPACHO_MEDICO);
        var admin=new Identidad(2L,"admin",1,"Administrador",Rol.ADMIN);
        Menu inventario=menu("/inicio/medicamentos");
        Menu modulo=menu("/inicio/despacho");
        Menu citas=menu("/inicio/citas");
        when(menus.obtenerMenusPorUsuario(1L)).thenReturn(List.of(inventario,modulo,citas),List.of(inventario));
        when(menus.obtenerMenusPorUsuario(2L)).thenReturn(List.of(modulo,citas));
        assertEquals(List.of(inventario,modulo),controlador.obtenerMenusPropios(despacho).getBody());
        assertEquals(List.of(inventario),controlador.obtenerMenusPropios(despacho).getBody());
        assertEquals(List.of(citas),controlador.obtenerMenusPropios(admin).getBody());
    }

    private Menu menu(String url) {
        var m=new Menu();
        m.setUrl(url);
        return m;
    }
}
