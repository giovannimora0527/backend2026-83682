
package com.uniminuto.clinica.service;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// SERVICIO DE GESTIÓN DE MÉDICOS VETERINARIOS
// ==========================================================

// Entidad que representa a los médicos.
import com.uniminuto.clinica.entity.Medico;

// Permite devolver un listado de médicos.
import java.util.List;

/**
 * ==========================================================
 * INTERFAZ: MedicoService
 * ==========================================================
 *
 * Define las operaciones del módulo Gestión de Médicos.
 *
 * Esta interfaz será implementada posteriormente
 * por la clase MedicoServiceImpl.
 *
 * Objetivo:
 *
 * Consultar los médicos registrados en MySQL
 * para mostrarlos en el formulario de Gestión de Citas.
 *
 * El usuario podrá seleccionar un veterinario
 * por sus nombres y apellidos, sin escribir su ID.
 */
public interface MedicoService {

    // ======================================================
    // 1. LISTAR MÉDICOS VETERINARIOS
    // ======================================================

    /**
     * Consulta todos los médicos registrados
     * en la base de datos clinica.
     *
     * Los resultados estarán ordenados
     * alfabéticamente por apellidos y nombres.
     *
     * @return Lista de médicos veterinarios.
     */
    List<Medico> listarMedicos();

}
