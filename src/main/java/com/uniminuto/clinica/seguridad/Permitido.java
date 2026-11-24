package com.uniminuto.clinica.seguridad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declara los perfiles autorizados para un endpoint (o para todos los endpoints de una API).
 * El {@link AutenticacionInterceptor} valida la anotación en el backend antes de ejecutar el
 * controlador, de modo que los permisos no dependen de lo que muestre la interfaz.
 *
 * <p>Los endpoints sin anotación solo exigen una sesión válida.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permitido {

    /** Códigos de perfil autorizados (ver {@link Perfiles}). */
    String[] value();
}
