package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.ConfiguracionClinica;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ConfiguracionClinicaRepository extends JpaRepository<ConfiguracionClinica, Long> {

    Optional<ConfiguracionClinica> findFirstByOrderById();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConfiguracionClinica c")
    List<ConfiguracionClinica> bloquear();
}
