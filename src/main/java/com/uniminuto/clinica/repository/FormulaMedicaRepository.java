
package com.uniminuto.clinica.repository;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// REPOSITORIO DE FÓRMULAS MÉDICAS
// ==========================================================

// Entidad que representa la tabla formula_medica.
import com.uniminuto.clinica.entity.FormulaMedica;

// Manejo de listas.
import java.util.List;

// Repositorio de Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// ==========================================================
// REPOSITORIO
// ==========================================================

public interface FormulaMedicaRepository
        extends JpaRepository<FormulaMedica, Long> {

    // ======================================================
    // 1. LISTAR FÓRMULAS POR ID ASCENDENTE
    // ======================================================

    /**
     * Ordena las fórmulas desde el ID menor
     * hasta el ID mayor.
     */
    List<FormulaMedica> findAllByOrderByIdAsc();

    // ======================================================
    // 2. LISTAR FÓRMULAS POR FECHA DESCENDENTE
    // ======================================================

    /**
     * Este método es utilizado por
     * FormulaMedicaServiceImpl.java.
     *
     * Ordena las fórmulas desde la fecha
     * de creación más reciente hasta la más antigua.
     *
     * Con esto corregimos el error de compilación
     * que aparece en la línea 35 del servicio.
     */
    List<FormulaMedica>
    findAllByOrderByFechaCreacionRegistroDesc();

}
