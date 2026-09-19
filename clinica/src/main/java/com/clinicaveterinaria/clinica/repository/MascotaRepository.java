package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByClienteId(Integer clienteId);
    List<Mascota> findByRazaId(Integer razaId);
}