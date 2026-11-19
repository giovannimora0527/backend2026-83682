
package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Permite consultar los usuarios
 * almacenados en MySQL.
 */
@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre.
     *
     * Se utilizará durante el inicio
     * de sesión para verificar
     * que el usuario existe.
     */
    Optional<Usuario> findByUsername(
            String username
    );
}
