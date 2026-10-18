package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a datos de veterinarios.
 */
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    /**
     * Lista los veterinarios activos ordenados por nombres y apellidos.
     *
     * @return veterinarios activos
     */
    List<Veterinario> findByActivoTrueOrderByNombresAscApellidosAsc();
}
