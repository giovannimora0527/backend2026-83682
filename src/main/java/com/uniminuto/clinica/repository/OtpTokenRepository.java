package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.OtpToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /** Bloquea la fila para que dos validaciones simultaneas no usen el mismo OTP. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpToken> findFirstByUsuarioIdOrderByIdDesc(Long usuarioId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM OtpToken o WHERE o.usuarioId = :usuarioId")
    void eliminarPorUsuario(@Param("usuarioId") Long usuarioId);
}
