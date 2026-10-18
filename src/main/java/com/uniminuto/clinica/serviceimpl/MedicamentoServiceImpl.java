
package com.uniminuto.clinica.serviceimpl;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// IMPLEMENTACIÓN DEL SERVICIO DE MEDICAMENTOS
// ==========================================================

// Entidad que representa los medicamentos de MySQL.
import com.uniminuto.clinica.entity.Medicamento;

// Repositorio encargado de consultar la base de datos.
import com.uniminuto.clinica.repository.MedicamentoRepository;

// Interfaz donde se define el método listarMedicamentos().
import com.uniminuto.clinica.service.MedicamentoService;

// Permite trabajar con listas de medicamentos.
import java.util.List;

// Identifica esta clase como un servicio de Spring Boot.
import org.springframework.stereotype.Service;

/**
 * ==========================================================
 * SERVICIO DE GESTIÓN DE MEDICAMENTOS
 * ==========================================================
 *
 * Esta clase implementa la interfaz MedicamentoService.
 *
 * Su función es consultar los medicamentos
 * almacenados en la base de datos MySQL.
 *
 * Base de datos: clinica
 * Tabla: medicamento
 *
 * Funcionamiento:
 *
 * 1. Recibe una solicitud del controlador.
 * 2. Utiliza MedicamentoRepository.
 * 3. Consulta los medicamentos registrados.
 * 4. Ordena los registros alfabéticamente.
 * 5. Devuelve la lista de medicamentos.
 */
@Service
public class MedicamentoServiceImpl
        implements MedicamentoService {

    // ======================================================
    // 1. REPOSITORIO DE MEDICAMENTOS
    // ======================================================

    /**
     * Repositorio utilizado para acceder
     * a los datos de la tabla medicamento.
     */
    private final MedicamentoRepository medicamentoRepository;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot inyecta automáticamente
     * el repositorio de medicamentos.
     *
     * @param medicamentoRepository
     * Repositorio encargado de consultar MySQL.
     */
    public MedicamentoServiceImpl(
            MedicamentoRepository medicamentoRepository) {

        this.medicamentoRepository = medicamentoRepository;

    }

    // ======================================================
    // 3. MÉTODO: LISTAR MEDICAMENTOS
    // ======================================================

    /**
     * Consulta todos los medicamentos registrados.
     *
     * El método utiliza el repositorio para obtener
     * los medicamentos ordenados por nombre.
     *
     * Ejemplos:
     *
     * - Paracetamol
     * - Paracetamol Inyectable
     * - Paracetamol Solución
     *
     * @return Lista de medicamentos ordenados.
     */
    @Override
    public List<Medicamento> listarMedicamentos() {

        // Ejecutar la consulta mediante Spring Data JPA.
        return medicamentoRepository
                .findAllByOrderByNombreAsc();

    }

}
