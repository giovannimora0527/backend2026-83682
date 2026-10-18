package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Acceso a datos de historias clínicas.
 */
public interface HistoriaClinicaRepository
        extends JpaRepository<HistoriaClinica, Long>, JpaSpecificationExecutor<HistoriaClinica> {

    /**
     * Indica si una cita ya generó una historia clínica.
     *
     * @param citaId identificador de la cita
     * @return {@code true} si existe una historia para esa cita
     */
    boolean existsByCita_Id(Long citaId);
}
