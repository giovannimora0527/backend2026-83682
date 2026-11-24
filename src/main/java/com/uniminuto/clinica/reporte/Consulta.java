package com.uniminuto.clinica.reporte;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;
import java.util.List;

/** Ejecuta JPQL con agregaciones y devuelve filas sueltas, sin cargar entidades. */
@Component
public class Consulta {

    private final EntityManager entityManager;

    public Consulta(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /** @param parametros pares nombre, valor. */
    public List<Object[]> filas(String jpql, Object... parametros) {
        TypedQuery<Object[]> consulta = entityManager.createQuery(jpql, Object[].class);
        for (int i = 0; i < parametros.length; i += 2) {
            consulta.setParameter((String) parametros[i], parametros[i + 1]);
        }
        return consulta.getResultList();
    }

    /** Columnas de periodo para SELECT y GROUP BY; siempre son tres. */
    public static String periodo(String campo, String agrupacion) {
        return switch (agrupacion) {
            case "DIA" -> "year(%1$s), month(%1$s), day(%1$s)".formatted(campo);
            case "ANIO" -> "year(%1$s), year(%1$s), year(%1$s)".formatted(campo);
            default -> "year(%1$s), month(%1$s), month(%1$s)".formatted(campo);
        };
    }

    public static String nombrePeriodo(Object[] fila, String agrupacion) {
        int anio = ((Number) fila[0]).intValue();
        int mes = ((Number) fila[1]).intValue();
        int dia = ((Number) fila[2]).intValue();
        return switch (agrupacion) {
            case "DIA" -> "%04d-%02d-%02d".formatted(anio, mes, dia);
            case "ANIO" -> "%04d".formatted(anio);
            default -> "%04d-%02d".formatted(anio, mes);
        };
    }
}
