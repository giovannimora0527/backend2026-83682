package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

@Repository

public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    List<Mascota> findAllByRaza(Raza raza);

    List<Mascota> findAllByCliente(Cliente cliente);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Mascota m where m.mascotaId = :id")
    Optional<Mascota> lockById(@org.springframework.data.repository.query.Param("id") Integer id);
}
