package com.uniminuto.clinica.service;

import com.uniminuto.clinica.model.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las fórmulas médicas.
 * Implementa la lógica de negocio para operaciones con fórmulas médicas.
 *
 * @author TuNombre
 * @version 1.0
 */
@Service
public class FormulaMedicaService {

    /**
     * Repositorio para acceder a datos de fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * Obtiene todas las fórmulas médicas ordenadas por fecha de creación
     * de la más reciente a la más antigua.
     *
     * @return Lista de fórmulas médicas ordenadas.
     */
    public List<FormulaMedica> listarTodasOrdenadasPorFecha() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    /**
     * Busca una fórmula médica por su identificador.
     *
     * @param id Identificador de la fórmula médica.
     * @return Optional conteniendo la fórmula médica si existe.
     */
    public Optional<FormulaMedica> buscarPorId(Long id) {
        return formulaMedicaRepository.findById(id);
    }

    /**
     * Guarda o actualiza una fórmula médica en el sistema.
     *
     * @param formulaMedica La fórmula médica a guardar.
     * @return La fórmula médica guardada con sus datos actualizados.
     */
    public FormulaMedica guardar(FormulaMedica formulaMedica) {
        return formulaMedicaRepository.save(formulaMedica);
    }
}