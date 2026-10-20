
package com.uniminuto.clinica.repository;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// REPOSITORIO DE HISTORIAS MÉDICAS
// ==========================================================

// Importar la entidad HistoriaMedica.
import com.uniminuto.clinica.entity.HistoriaMedica;

// Importar JpaRepository de Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importar List para manejar varios registros.
import java.util.List;

// ==========================================================
// INTERFAZ DEL REPOSITORIO
// ==========================================================

/**
 * Permite consultar los datos de la tabla
 * historia_medica mediante Spring Data JPA.
 *
 * JpaRepository proporciona operaciones como:
 *
 * - findAll(): consultar todos los registros.
 * - findById(): consultar por identificador.
 * - save(): guardar o actualizar.
 * - deleteById(): eliminar por identificador.
 */

public interface HistoriaMedicaRepository
        extends JpaRepository<HistoriaMedica, Long> {

    // ======================================================
    // 1. LISTAR HISTORIAS MÉDICAS ORDENADAS POR ID
    // ======================================================

    /**
     * Consulta las historias médicas de la base de datos
     * y las organiza por ID de menor a mayor.
     *
     * @return Lista de historias médicas.
     */

    List<HistoriaMedica> findAllByOrderByIdAsc();

    // ======================================================
    // 2. BUSCAR HISTORIAS POR PACIENTE
    // ======================================================

    /**
     * Consulta todas las historias médicas
     * relacionadas con un paciente.
     *
     * @param pacienteId Identificador del paciente.
     * @return Lista de historias del paciente.
     */

    List<HistoriaMedica> findByPacienteId(Integer pacienteId);

}
