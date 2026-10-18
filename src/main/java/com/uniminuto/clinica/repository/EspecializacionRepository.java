
package com.uniminuto.clinica.repository;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// REPOSITORIO DE ESPECIALIZACIONES
// ==========================================================

// Entidad que representa la tabla especializacion.
import com.uniminuto.clinica.entity.Especializacion;

// Spring Data JPA permite consultar MySQL.
import org.springframework.data.jpa.repository.JpaRepository;

// Identifica esta interfaz como repositorio.
import org.springframework.stereotype.Repository;

// Permite devolver listas de registros.
import java.util.List;

/**
 * ==========================================================
 * REPOSITORIO DE GESTIÓN DE ESPECIALIZACIONES
 * ==========================================================
 *
 * Permite consultar los registros de la tabla:
 *
 * Base de datos: clinica
 * Tabla: especializacion
 * Llave primaria: id
 *
 * JpaRepository proporciona automáticamente:
 *
 * - findAll(): listar todos los registros.
 * - findById(): buscar una especialización por ID.
 * - save(): guardar o actualizar registros.
 * - deleteById(): eliminar un registro.
 * - count(): contar especializaciones.
 *
 * Utilizamos Long porque la llave primaria
 * de MySQL es BIGINT UNSIGNED.
 */
@Repository
public interface EspecializacionRepository
        extends JpaRepository<Especializacion, Long> {

    // ======================================================
    // 1. LISTAR ESPECIALIZACIONES ORDENADAS
    // ======================================================

    /**
     * Consulta todas las especializaciones
     * y las ordena alfabéticamente por nombre.
     *
     * Ejemplos:
     *
     * - Anestesiología
     * - Cardiología
     * - Dermatología
     * - Neurología
     *
     * Spring Data JPA genera la consulta SQL
     * automáticamente.
     *
     * @return Lista de especializaciones ordenadas.
     */
    List<Especializacion> findAllByOrderByNombreAsc();

}
