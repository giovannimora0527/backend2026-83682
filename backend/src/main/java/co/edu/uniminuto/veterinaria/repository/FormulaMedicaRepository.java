package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a datos de fórmulas médicas.
 */
public interface FormulaMedicaRepository
        extends JpaRepository<FormulaMedica, Long>, JpaSpecificationExecutor<FormulaMedica> {

    /**
     * Cuenta las fórmulas activas de una historia con el mismo medicamento (evita duplicidad).
     *
     * @param historiaId     historia clínica
     * @param medicamentoId  medicamento
     * @param excluirId      fórmula que se ignora (la que se edita; -1 si no aplica)
     * @return cantidad de fórmulas activas duplicadas
     */
    @Query("select count(f) from FormulaMedica f where f.historia.id = :historiaId "
            + "and f.medicamento.id = :medicamentoId and f.activo = true and f.id <> :excluirId")
    long contarActivasDuplicadas(@Param("historiaId") Long historiaId,
                                 @Param("medicamentoId") Long medicamentoId,
                                 @Param("excluirId") Long excluirId);
}
