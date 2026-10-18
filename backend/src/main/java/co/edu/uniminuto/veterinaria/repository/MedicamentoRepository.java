package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Acceso a datos de medicamentos.
 */
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    /**
     * Lista los medicamentos que no están vencidos a la fecha indicada.
     *
     * @param hoy fecha de referencia
     * @return medicamentos vigentes ordenados por nombre
     */
    List<Medicamento> findByFechaVenceGreaterThanEqualOrderByNombreAsc(LocalDate hoy);
}
