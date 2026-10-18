package co.edu.uniminuto.veterinaria.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Funciones auxiliares reutilizables para construir los filtros dinámicos (Specifications) de los listados.
 */
public final class Especificaciones {

    /** Evita instanciar la clase utilitaria. */
    private Especificaciones() {
    }

    /**
     * Indica si una cadena trae texto útil para filtrar.
     *
     * @param texto cadena a revisar
     * @return {@code true} si no es nula ni está en blanco
     */
    public static boolean hayTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    /**
     * Construye una condición "contiene" sin distinguir mayúsculas.
     *
     * @param cb    constructor de criterios
     * @param campo expresión de texto de la entidad
     * @param texto texto buscado
     * @return predicado LIKE '%texto%'
     */
    public static Predicate contiene(CriteriaBuilder cb, Expression<String> campo, String texto) {
        return cb.like(cb.lower(campo), "%" + texto.trim().toLowerCase() + "%");
    }

    /**
     * Construye la expresión "nombres apellidos" de una persona (cliente o veterinario).
     *
     * @param cb      constructor de criterios
     * @param persona ruta de la entidad que tiene los campos {@code nombres} y {@code apellidos}
     * @return expresión con el nombre completo
     */
    public static Expression<String> nombreCompleto(CriteriaBuilder cb, Path<?> persona) {
        return cb.concat(cb.concat(persona.<String>get("nombres"), " "), persona.<String>get("apellidos"));
    }

    /**
     * Devuelve el inicio del día (00:00) de una fecha.
     *
     * @param fecha fecha
     * @return fecha y hora a las 00:00
     */
    public static LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay();
    }

    /**
     * Devuelve el inicio del día siguiente (límite superior exclusivo para filtros "hasta").
     *
     * @param fecha fecha
     * @return fecha y hora a las 00:00 del día siguiente
     */
    public static LocalDateTime inicioDelDiaSiguiente(LocalDate fecha) {
        return fecha.plusDays(1).atStartOfDay();
    }
}
