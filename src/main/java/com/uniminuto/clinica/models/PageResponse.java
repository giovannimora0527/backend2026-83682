package com.uniminuto.clinica.models;

import org.springframework.data.domain.Page;
import java.util.List;

/** Respuesta estándar de una consulta paginada. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> from(Page<T> result) {
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
