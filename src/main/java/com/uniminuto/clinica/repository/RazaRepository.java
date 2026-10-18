
package com.uniminuto.clinica.repository;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// REPOSITORIO DE RAZAS
// ==========================================================

// Entidad que representa la tabla raza.
import com.uniminuto.clinica.entity.Raza;

// Spring Data JPA permite realizar consultas a MySQL.
import org.springframework.data.jpa.repository.JpaRepository;

// Identifica esta interfaz como repositorio.
import org.springframework.stereotype.Repository;

// Permite devolver listas de registros.
import java.util.List;

/**
 * Repositorio para consultar la tabla "raza".
 *
 * Base de datos: clinica
 * Tabla: raza
 * Llave primaria: raza_id
 *
 * JpaRepository proporciona automáticamente:
 *
 * - findAll(): consultar todas las razas.
 * - findById(): consultar una raza por ID.
 * - save(): guardar o actualizar una raza.
 * - deleteById(): eliminar una raza.
 * - count(): contar registros.
 */

@Repository
public interface RazaRepository
        extends JpaRepository<Raza, Integer> {

    // ======================================================
    // MÉTODO 1. LISTAR RAZAS ORDENADAS
    // ======================================================

    /**
     * Consulta las razas registradas en MySQL
     * y las ordena alfabéticamente por nombre.
     *
     * Spring Data JPA genera la consulta
     * automáticamente a partir del nombre
     * del método.
     *
     * Ejemplo:
     *
     * Gato - Felino
     * Perro - Canino
     *
     * @return Lista de razas ordenadas.
     */
    List<Raza> findAllByOrderByNombreAsc();

}
