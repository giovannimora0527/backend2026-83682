
package com.uniminuto.clinica.service;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// INTERFAZ DEL SERVICIO DE ESPECIALIZACIONES
// ==========================================================

// Entidad relacionada con la tabla especializacion.
import com.uniminuto.clinica.entity.Especializacion;

// Permite trabajar con listas de registros.
import java.util.List;

/**
 * ==========================================================
 * SERVICIO DE GESTIÓN DE ESPECIALIZACIONES
 * ==========================================================
 *
 * Esta interfaz define los métodos que utilizará
 * el módulo de Gestión de Especializaciones.
 *
 * Base de datos: clinica
 * Tabla: especializacion
 *
 * Columnas:
 * - id
 * - nombre
 * - descripcion
 * - codigo_especializacion
 *
 * La implementación de los métodos se realizará
 * en EspecializacionServiceImpl.java.
 */
public interface EspecializacionService {

    // ======================================================
    // MÉTODO 1: LISTAR ESPECIALIZACIONES
    // ======================================================

    /**
     * Obtiene todas las especializaciones registradas
     * en la base de datos.
     *
     * Los resultados estarán ordenados
     * alfabéticamente por nombre.
     *
     * Ejemplos:
     * - Anestesiología
     * - Cardiología
     * - Dermatología
     * - Neurología
     *
     * @return Lista de especializaciones.
     */
    List<Especializacion> listarEspecializaciones();

}
