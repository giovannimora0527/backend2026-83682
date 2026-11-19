
package com.uniminuto.clinica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * CLÍNICA VETERINARIA - UNIMINUTO
 *
 * Configuración del cifrado de contraseñas.
 *
 * BCrypt permite proteger las contraseñas
 * antes de almacenarlas en MySQL.
 */
@Configuration
public class SecurityCryptoConfig {

    /**
     * Crea un componente reutilizable para:
     *
     * 1. Cifrar contraseñas nuevas.
     * 2. Validar contraseñas al iniciar sesión.
     *
     * El valor 12 representa el factor de costo.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder(12);

    }

}
