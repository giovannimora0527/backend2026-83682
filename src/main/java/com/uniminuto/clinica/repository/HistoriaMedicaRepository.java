package com.uniminuto.clinica.repository;
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface HistoriaMedicaRepository
        extends JpaRepository<HistoriaMedica,Long>{
    List<HistoriaMedica> findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal ) ;
}
