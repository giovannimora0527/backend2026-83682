package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.CargaInventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CargaInventarioRepository extends JpaRepository<CargaInventario, Long> {

    @EntityGraph(attributePaths = {"usuario", "usuarioConfirmacion"})
    List<CargaInventario> findByFechaCargaGreaterThanEqualAndFechaCargaLessThanOrderByFechaCargaDesc(
            LocalDateTime desde, LocalDateTime hasta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CargaInventario c WHERE c.id = :id")
    Optional<CargaInventario> bloquear(@Param("id") Long id);
}
