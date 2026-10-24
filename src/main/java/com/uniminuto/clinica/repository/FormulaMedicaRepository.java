package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroAsc();

    List<FormulaMedica> findByCita_IdInOrderByFechaCreacionRegistroDesc(List<Long> citaIds);

    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();
}
