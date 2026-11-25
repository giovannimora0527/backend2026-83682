package com.uniminuto.clinica.menu;

import java.time.LocalDateTime;

/**
 * DTO de una opción del menú lateral.
 *
 * @param menuId identificador.
 * @param code código de la opción.
 * @param title título visible.
 * @param url ruta del frontend.
 * @param icon clase del ícono.
 * @param activo estado.
 * @param fechaCreacion fecha de creación.
 * @param fechaModificacion fecha de modificación.
 */
public record MenuDto(int menuId, String code, String title, String url, String icon, boolean activo,
                      LocalDateTime fechaCreacion, LocalDateTime fechaModificacion) {
}
