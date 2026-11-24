package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    @EntityGraph(attributePaths = {"medico", "mascota", "cliente"})
    List<FormulaMedica> findAllByOrderByFechaGeneracionDesc();

    @EntityGraph(attributePaths = {"medico", "mascota", "cliente"})
    List<FormulaMedica> findByMedico_IdOrderByFechaGeneracionDesc(Long medicoId);

    @EntityGraph(attributePaths = {"medico", "mascota", "cliente"})
    List<FormulaMedica> findByCita_IdInOrderByFechaGeneracionDesc(List<Long> citaIds);
}
