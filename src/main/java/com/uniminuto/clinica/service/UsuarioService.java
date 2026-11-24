package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;

import java.util.List;

public interface UsuarioService {

    List<UsuarioRs> obtenerUsuariosOrdenados();

    MiRespuestaRS crearUsuario(UsuarioRq usuarioRq);

    MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq);
}
