package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.AnotacionHistoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Acceso a datos de anotaciones de historias clínicas.
 */
public interface AnotacionHistoriaRepository
        extends JpaRepository<AnotacionHistoria, Long>, JpaSpecificationExecutor<AnotacionHistoria> {

    /**
     * Busca una anotación verificando que pertenezca a la historia indicada.
     *
     * @param id          identificador de la anotación
     * @param historiaId  identificador de la historia
     * @return la anotación, si existe y pertenece a la historia
     */
    Optional<AnotacionHistoria> findByIdAndHistoria_Id(Long id, Long historiaId);
}
