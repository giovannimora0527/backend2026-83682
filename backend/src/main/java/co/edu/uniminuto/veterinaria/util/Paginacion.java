package co.edu.uniminuto.veterinaria.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Utilidad para construir {@link Pageable} de forma segura: el ordenamiento solo puede hacerse por
 * columnas permitidas (lista blanca), lo que evita que el cliente ordene por campos arbitrarios.
 */
public final class Paginacion {

    /** Tamaño máximo de página aceptado. */
    private static final int TAMANO_MAXIMO = 100;

    /** Evita instanciar la clase utilitaria. */
    private Paginacion() {
    }

    /**
     * Construye el {@link Pageable} a partir de los parámetros recibidos en la URL.
     *
     * @param pagina               número de página (base 0)
     * @param tamano               registros por página (se limita a 1..100)
     * @param orden                nombre lógico de la columna por la que ordenar (puede ser nulo)
     * @param direccion            "asc" o "desc" (puede ser nulo)
     * @param camposPermitidos     mapa nombre lógico -> propiedad de la entidad
     * @param ordenPorDefecto      propiedad de la entidad usada si {@code orden} no es válido
     * @param direccionPorDefecto  dirección usada si {@code direccion} no es válida
     * @return paginación con ordenamiento y desempate por id
     */
    public static Pageable crear(int pagina, int tamano, String orden, String direccion,
                                 Map<String, String> camposPermitidos, String ordenPorDefecto,
                                 Sort.Direction direccionPorDefecto) {
        int paginaSegura = Math.max(pagina, 0);
        int tamanoSeguro = Math.min(Math.max(tamano, 1), TAMANO_MAXIMO);

        String propiedad = orden != null && camposPermitidos.containsKey(orden)
                ? camposPermitidos.get(orden)
                : ordenPorDefecto;

        Sort.Direction sentido = direccionPorDefecto;
        if ("asc".equalsIgnoreCase(direccion)) {
            sentido = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(direccion)) {
            sentido = Sort.Direction.DESC;
        }

        // El desempate por id mantiene estable el orden entre páginas.
        Sort ordenamiento = Sort.by(new Sort.Order(sentido, propiedad)).and(Sort.by(sentido, "id"));
        return PageRequest.of(paginaSegura, tamanoSeguro, ordenamiento);
    }
}
