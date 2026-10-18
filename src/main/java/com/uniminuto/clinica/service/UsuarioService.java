package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;

import java.util.List;

public interface UsuarioService {

    /** Lista los usuarios ordenados alfabéticamente, sin exponer el hash de la contraseña. */
    List<UsuarioRs> obtenerUsuariosOrdenados();

    /** Crea un usuario nuevo; la contraseña es obligatoria. */
    MiRespuestaRS crearUsuario(UsuarioRq usuarioRq);

    /** Actualiza un usuario; si no se envía contraseña se conserva la actual. */
    MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq);
}
