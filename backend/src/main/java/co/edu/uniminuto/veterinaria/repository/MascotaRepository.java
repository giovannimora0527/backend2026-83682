package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a datos de mascotas.
 */
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    /**
     * Lista las mascotas activas de un cliente ordenadas por nombre.
     *
     * @param clienteId identificador del cliente
     * @return mascotas activas del cliente
     */
    List<Mascota> findByCliente_IdAndActivoTrueOrderByNombreAsc(Long clienteId);
}
