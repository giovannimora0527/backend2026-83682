package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    Optional<Medicamento> findByCodigo(String codigo);

    Optional<Medicamento> findByNombreIgnoreCase(String nombre);

    @Query("SELECT m FROM Medicamento m WHERE m.codigo IN :codigos OR LOWER(m.nombre) IN :nombres")
    List<Medicamento> buscarPorCodigosONombres(@Param("codigos") Collection<String> codigos,
                                               @Param("nombres") Collection<String> nombres);

    /** Serializa los cambios de existencias de un producto. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medicamento m WHERE m.id = :id")
    Optional<Medicamento> bloquear(@Param("id") Long id);
}
