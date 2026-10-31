package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Menu;
import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.entity.PerfilMenu;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.PerfilMenuRepository;
import com.uniminuto.clinica.repository.PerfilRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private PerfilMenuRepository perfilMenuRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Menu> obtenerListaMenus(Long usuarioId) {
        List<Menu> menus = new ArrayList<>();
        Optional<Usuario> optUser = usuarioRepository.findById(usuarioId);
        if (optUser.isPresent()) {
            Usuario usuario = optUser.get();
            Optional<Perfil> perfil = perfilRepository.findById(usuario.getPerfil().getPerfilId());
            if (perfil.isPresent()) {
                List<PerfilMenu> perfilMenus = perfilMenuRepository.findByPerfil(perfil.get());
                for (PerfilMenu perfilMenu : perfilMenus) {
                    Menu menu = perfilMenu.getMenu();
                    if (!menus.contains(menu)) {
                        menus.add(menu);
                    }
                }
            }
        }
        return menus;
    }
}
