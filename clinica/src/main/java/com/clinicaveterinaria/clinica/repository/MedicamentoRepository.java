<<<<<<< Updated upstream:clinica/src/main/java/com/clinicaveterinaria/clinica/repository/MedicamentoRepository.java
package com.clinicaveterinaria.clinica.repository;

import com.clinicaveterinaria.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    Optional<Medicamento> findByNombre(String nombre);
=======
package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

>>>>>>> Stashed changes:src/main/java/com/uniminuto/clinica/repository/MedicamentoRepository.java
}