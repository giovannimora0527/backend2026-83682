package co.edu.uniminuto.veterinaria.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Respuesta paginada genérica usada por todos los listados.
 *
 * @param contenido      registros de la página actual
 * @param pagina         número de página (base 0)
 * @param tamano         registros por página
 * @param totalElementos total de registros que cumplen el filtro
 * @param totalPaginas   total de páginas
 * @param <T>            tipo de los registros
 */
public record PageResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    /**
     * Convierte una página de Spring Data en la respuesta de la API.
     *
     * @param page página de Spring Data
     * @param <T>  tipo de los registros
     * @return respuesta paginada
     */
    public static <T> PageResponse<T> de(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
