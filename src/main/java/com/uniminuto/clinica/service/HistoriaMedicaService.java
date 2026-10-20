
package com.uniminuto.clinica.service;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// SERVICIO DE HISTORIAS MÉDICAS
// ==========================================================

// Entidad de la tabla historia_medica.
import com.uniminuto.clinica.entity.HistoriaMedica;

// Permite retornar varios registros.
import java.util.List;

// ==========================================================
// INTERFAZ DEL SERVICIO
// ==========================================================

/**
 * Define las operaciones que estarán disponibles
 * para gestionar las historias médicas.
 *
 * Su implementación se realizará en
 * HistoriaMedicaServiceImpl.java.
 */

public interface HistoriaMedicaService {

    // ======================================================
    // 1. LISTAR TODAS LAS HISTORIAS MÉDICAS
    // ======================================================

    /**
     * Obtiene todas las historias médicas.
     *
     * @return Lista de historias médicas.
     */

    List<HistoriaMedica> obtenerHistoriasMedicas();

    // ======================================================
    // 2. CONSULTAR HISTORIAS POR PACIENTE
    // ======================================================

    /**
     * Busca las historias médicas relacionadas
     * con un paciente específico.
     *
     * @param pacienteId Identificador del paciente.
     * @return Historias del paciente consultado.
     */

    List<HistoriaMedica> obtenerHistoriasPorPaciente(
            Integer pacienteId
    );

}
