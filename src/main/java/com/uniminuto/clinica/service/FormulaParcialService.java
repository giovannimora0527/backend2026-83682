package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaParcialRq;
import java.util.List;
/** Creación y consulta de fórmulas asociadas a citas. */
public interface FormulaParcialService {
    List<FormulaMedica> listar();
    FormulaMedica obtener(Long id);
    FormulaMedica guardar(FormulaParcialRq datos);
    FormulaMedica actualizar(Long id, FormulaParcialRq datos);
}
