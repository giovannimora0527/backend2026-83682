package com.uniminuto.clinica.repository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    List<Mascota> findAllByOrderByNombreMascotaAsc();

    List<Mascota> findAllByOrderByNombreMascotaDesc();

    List<Mascota> findByNombreMascota(String nombreMascota);

    List<Mascota> findByClienteOrderByNombreMascotaAsc(Cliente c);

    List<Mascota> findByRazaOrderByNombreMascotaAsc(Raza raza);

    /** Serializa el agendamiento. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT x FROM Mascota x WHERE x.mascotaId = :id")
    Optional<Mascota> bloquear(@Param("id") Integer id);
}
