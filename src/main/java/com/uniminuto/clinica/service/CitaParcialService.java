package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaParcialRq;
import java.util.List;
/** Operaciones de programación y consulta de citas. */
public interface CitaParcialService {
    List<Cita> listar();
    Cita obtener(Long id);
    Cita guardar(CitaParcialRq datos);
    Cita actualizar(Long id, CitaParcialRq datos);
}
