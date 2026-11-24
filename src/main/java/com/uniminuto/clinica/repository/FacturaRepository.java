package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.enums.EstadoFactura;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    @EntityGraph(attributePaths = {"usuario", "usuarioAnulacion"})
    @Query("""
            SELECT f FROM Factura f
            WHERE (:estado IS NULL OR f.estado = :estado)
              AND (:clienteId IS NULL OR f.cliente.id = :clienteId)
              AND (:mascotaId IS NULL OR f.mascota.mascotaId = :mascotaId)
              AND f.fechaGeneracion >= :desde AND f.fechaGeneracion < :hasta
            ORDER BY f.fechaGeneracion DESC, f.id DESC
            """)
    List<Factura> buscar(@Param("estado") EstadoFactura estado, @Param("clienteId") Long clienteId,
                         @Param("mascotaId") Integer mascotaId, @Param("desde") LocalDateTime desde,
                         @Param("hasta") LocalDateTime hasta);

    boolean existsByCita_IdAndEstadoNot(Long citaId, EstadoFactura estado);

    /** Citas atendidas, con atencion finalizada y sin factura vigente. */
    @EntityGraph(attributePaths = {"cliente", "mascota", "medico"})
    @Query("""
            SELECT c FROM Cita c
            WHERE c.estado = com.uniminuto.clinica.entity.enums.EstadoCita.ATENDIDA
              AND EXISTS (SELECT a.id FROM AtencionMedica a WHERE a.cita = c AND a.finalizada = true)
              AND NOT EXISTS (SELECT f.id FROM Factura f WHERE f.cita = c
                  AND f.estado <> com.uniminuto.clinica.entity.enums.EstadoFactura.ANULADA)
            ORDER BY c.fechaInicio DESC
            """)
    List<Cita> citasPendientes();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Factura f WHERE f.id = :id")
    Optional<Factura> bloquear(@Param("id") Long id);
}
