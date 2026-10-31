package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> obtenerListaMenus(Long usuarioId);
}
