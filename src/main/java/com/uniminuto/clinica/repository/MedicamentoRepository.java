
package com.uniminuto.clinica.repository;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// REPOSITORIO DE GESTIÓN DE MEDICAMENTOS
// ==========================================================

// Entidad que representa la tabla medicamento.
import com.uniminuto.clinica.entity.Medicamento;

// Permite acceder a los datos mediante Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Identifica esta interfaz como repositorio.
import org.springframework.stereotype.Repository;

// Permite trabajar con listas de registros.
import java.util.List;

/**
 * ==========================================================
 * REPOSITORIO DE MEDICAMENTOS
 * ==========================================================
 *
 * Se encarga de acceder a los medicamentos
 * registrados en la base de datos.
 *
 * Base de datos: clinica
 * Tabla: medicamento
 *
 * JpaRepository proporciona métodos automáticos:
 *
 * - findAll(): consultar todos los medicamentos.
 * - findById(): consultar un medicamento por ID.
 * - save(): guardar o actualizar medicamentos.
 * - deleteById(): eliminar un medicamento.
 * - count(): contar medicamentos.
 *
 * Utilizamos Long porque la columna id
 * está definida como BIGINT UNSIGNED.
 */
@Repository
public interface MedicamentoRepository
        extends JpaRepository<Medicamento, Long> {

    // ======================================================
    // 1. LISTAR MEDICAMENTOS ORDENADOS POR NOMBRE
    // ======================================================

    /**
     * Obtiene todos los medicamentos registrados
     * y los organiza alfabéticamente por nombre.
     *
     * Ejemplos de medicamentos:
     *
     * - Paracetamol
     * - Paracetamol Inyectable
     * - Paracetamol Solución
     *
     * Spring Data JPA genera automáticamente
     * la consulta a partir del nombre del método.
     *
     * @return Lista de medicamentos ordenados.
     */
    List<Medicamento> findAllByOrderByNombreAsc();

}
