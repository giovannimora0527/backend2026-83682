
package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.AutenticacionService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Implementación del servicio de autenticación.
 *
 * Verifica:
 * 1. Que se ingresen las credenciales.
 * 2. Que el usuario exista.
 * 3. Que el usuario esté activo.
 * 4. Que la contraseña coincida con BCrypt.
 */
@Service
public class AutenticacionServiceImpl
        implements AutenticacionService {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public AutenticacionServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario validarCredenciales(
            String username,
            String password
    ) {

        // Verificar campos obligatorios.
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe ingresar usuario y contraseña."
            );
        }

        // Buscar el usuario en MySQL.
        Usuario usuario = usuarioRepository
                .findByUsername(username.trim())
                .orElseThrow(() -> credencialesInvalidas());

        // Verificar que esté activo.
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw credencialesInvalidas();
        }

        // Recuperar el hash almacenado.
        String hash = usuario.getPasswordHash();

        if (hash == null || hash.isBlank()) {
            throw credencialesInvalidas();
        }

        // Aceptar únicamente contraseñas BCrypt.
        if (!(hash.startsWith("$2a$")
                || hash.startsWith("$2b$")
                || hash.startsWith("$2y$"))) {

            throw credencialesInvalidas();
        }

        // Comparar la contraseña ingresada
        // con el hash almacenado.
        if (!passwordEncoder.matches(password, hash)) {
            throw credencialesInvalidas();
        }

        // La autenticación fue correcta.
        // Este objeto solo se usará internamente.
        // No debe devolverse directamente al frontend.
        return usuario;
    }

    private ResponseStatusException credencialesInvalidas() {

        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario o contraseña incorrectos."
        );
    }

}
