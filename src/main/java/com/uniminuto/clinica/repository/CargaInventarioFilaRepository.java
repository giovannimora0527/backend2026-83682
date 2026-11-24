package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.CargaInventarioFila;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CargaInventarioFilaRepository extends JpaRepository<CargaInventarioFila, Long> {

    List<CargaInventarioFila> findByCarga_IdOrderByNumeroFila(Long cargaId);

    @Query("SELECT f.id FROM CargaInventarioFila f WHERE f.carga.id = :cargaId AND f.valida = true "
            + "AND f.importada = false ORDER BY f.numeroFila")
    List<Long> pendientes(@Param("cargaId") Long cargaId);

    long countByCarga_IdAndImportadaTrue(Long cargaId);
}
