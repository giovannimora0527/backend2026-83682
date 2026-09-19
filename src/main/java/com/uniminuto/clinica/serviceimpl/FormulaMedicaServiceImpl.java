package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio encargado de gestionar
 * las operaciones relacionadas con las fórmulas médicas.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /**
     * Repositorio utilizado para consultar
     * las fórmulas médicas en la base de datos.
     */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * Obtiene todas las fórmulas médicas ordenadas
     * desde la fecha de creación más reciente
     * hasta la más antigua.
     *
     * @return lista de fórmulas médicas ordenadas.
     */
    @Override
    public List<FormulaMedica> obtenerFormulasMedicas() {
        return formulaMedicaRepository
                .findAllByOrderByFechaCreacionRegistroDesc();
    }
}