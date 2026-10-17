package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.data.domain.*;
import java.util.Set;

/** Valida y limita los parámetros de paginación y ordenamiento de la API. */
final class PageSupport {
    /** Impide crear instancias de esta clase de utilidades. */
    private PageSupport() { }

    /**
     * Valida los parámetros de paginación y ordenamiento y construye el {@link Pageable}.
     *
     * @param page índice de página, comenzando en cero
     * @param size cantidad de elementos por página (de 1 a 100)
     * @param sort campo solicitado; si falta, se usa {@code defaultSort}
     * @param direction dirección solicitada; si falta, se usa descendente
     * @param defaultSort campo de ordenamiento predeterminado
     * @param allowed campos permitidos para ordenar
     * @return configuración de paginación y ordenamiento
     * @throws BadRequestException si la página, el tamaño, el campo o la dirección no son válidos
     */
    static Pageable pageable(int page, int size, String sort, String direction, String defaultSort, Set<String> allowed) {
        if (page < 0 || size < 1 || size > 100) throw new BadRequestException("page debe ser >= 0 y size debe estar entre 1 y 100");
        String field = sort == null || sort.isBlank() ? defaultSort : sort;
        if (!allowed.contains(field)) throw new BadRequestException("Campo de ordenamiento no permitido: " + field);
        Sort.Direction dir;
        try { dir = direction == null ? Sort.Direction.DESC : Sort.Direction.fromString(direction); }
        catch (IllegalArgumentException ex) { throw new BadRequestException("direction debe ser ASC o DESC"); }
        return PageRequest.of(page, size, Sort.by(dir, field));
    }
    /**
     * Comprueba que una cadena tenga contenido no blanco.
     *
     * @param value valor que se valida
     * @param field nombre del campo usado en el mensaje de error
     * @throws BadRequestException si el valor es nulo o está vacío
     */
    static void required(String value, String field) {
        if (value == null || value.isBlank()) throw new BadRequestException(field + " es obligatorio");
    }
}
