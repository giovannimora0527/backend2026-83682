
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Servicio encargado de validar
 * las credenciales de los usuarios.
 */
public interface AutenticacionService {

    Usuario validarCredenciales(
            String username,
            String password
    );

}
