package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.*;
import java.util.List;
/** Consultas y anotaciones del historial médico. */
public interface HistoriaParcialService {
    List<HistoriaMedica> listar();
    HistoriaMedica obtener(Long id);
    HistoriaMedica guardar(HistoriaParcialRq datos);
    List<AnotacionHistoria> anotaciones(Long historiaId);
    AnotacionHistoria agregarAnotacion(Long historiaId, AnotacionParcialRq datos);
    AnotacionHistoria obtenerAnotacion(Long id);
}
