
package com.uniminuto.clinica.service;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// INTERFAZ DEL SERVICIO DE RAZAS
// ==========================================================

// Entidad que representa la tabla raza de MySQL.
import com.uniminuto.clinica.entity.Raza;

// Permite trabajar con listas de registros.
import java.util.List;

/**
 * ==========================================================
 * SERVICIO DE GESTIÓN DE RAZAS
 * ==========================================================
 *
 * Esta interfaz define las operaciones que
 * tendrá el módulo de Gestión de Razas.
 *
 * Por ahora implementaremos la consulta
 * de todos los registros de la tabla raza.
 *
 * La lógica se desarrollará posteriormente
 * en RazaServiceImpl.java.
 */
public interface RazaService {

    // ======================================================
    // MÉTODO 1: LISTAR TODAS LAS RAZAS
    // ======================================================

    /**
     * Consulta las razas almacenadas en MySQL.
     *
     * Ejemplo de resultados:
     *
     * 1 - Gato - Felino
     * 2 - Perro - Canino
     *
     * @return Lista de razas registradas.
     */
    List<Raza> listarRazas();

}
