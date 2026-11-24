package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {
    List<AnotacionHistoria> findByHistoria(HistoriaMedica historia);
}
