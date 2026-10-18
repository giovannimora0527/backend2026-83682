
package com.uniminuto.clinica.service;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// INTERFAZ DEL SERVICIO DE MEDICAMENTOS
// ==========================================================

// Entidad que representa la tabla medicamento.
import com.uniminuto.clinica.entity.Medicamento;

// Permite trabajar con listas de medicamentos.
import java.util.List;

/**
 * ==========================================================
 * SERVICIO DE GESTIÓN DE MEDICAMENTOS
 * ==========================================================
 *
 * Esta interfaz define las operaciones que
 * estarán disponibles para gestionar medicamentos.
 *
 * Base de datos: clinica
 * Tabla: medicamento
 *
 * Campos registrados:
 *
 * 1. id
 * 2. nombre
 * 3. descripcion
 * 4. presentacion
 * 5. fecha_compra
 * 6. fecha_vence
 * 7. fecha_creacion_registro
 * 8. fecha_modificacion_registro
 *
 * La implementación de esta interfaz se realizará
 * en MedicamentoServiceImpl.java.
 */
public interface MedicamentoService {

    // ======================================================
    // 1. MÉTODO PARA LISTAR MEDICAMENTOS
    // ======================================================

    /**
     * Consulta todos los medicamentos registrados
     * en la base de datos.
     *
     * Los resultados estarán ordenados
     * alfabéticamente por nombre.
     *
     * Ejemplos:
     *
     * - Paracetamol
     * - Paracetamol Inyectable
     * - Paracetamol Solución
     *
     * @return Lista de medicamentos registrados.
     */
    List<Medicamento> listarMedicamentos();

}
