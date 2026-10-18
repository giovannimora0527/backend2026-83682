
package com.uniminuto.clinica.repository;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// REPOSITORIO DE MÉDICOS VETERINARIOS
// ==========================================================

// Entidad que representa la tabla medico.
import com.uniminuto.clinica.entity.Medico;

// JPA proporciona operaciones para consultar y guardar datos.
import org.springframework.data.jpa.repository.JpaRepository;

// Anotación que identifica el repositorio.
import org.springframework.stereotype.Repository;

// Permite trabajar con listas de médicos.
import java.util.List;

/**
 * Repositorio encargado de acceder a los datos
 * de los médicos veterinarios registrados en MySQL.
 *
 * Base de datos: clinica
 * Tabla: medico
 *
 * JpaRepository proporciona automáticamente:
 *
 * - findAll(): consultar todos los médicos.
 * - findById(): consultar un médico por ID.
 * - existsById(): verificar si existe un médico.
 * - save(): guardar o actualizar un médico.
 * - deleteById(): eliminar un médico por ID.
 */
@Repository
public interface MedicoRepository
        extends JpaRepository<Medico, Long> {

    // ======================================================
    // 1. LISTAR MÉDICOS ORDENADOS ALFABÉTICAMENTE
    // ======================================================

    /**
     * Obtiene todos los médicos registrados.
     *
     * Los resultados se ordenan primero por apellido
     * y después por nombre, de forma ascendente.
     *
     * Spring Data JPA genera la consulta
     * automáticamente a partir del nombre del método.
     *
     * @return Lista de médicos ordenados.
     */
    List<Medico> findAllByOrderByApellidosAscNombresAsc();

}
