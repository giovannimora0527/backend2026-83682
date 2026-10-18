
package com.uniminuto.clinica.serviceimpl;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// IMPLEMENTACIÓN DEL SERVICIO DE ESPECIALIZACIONES
// ==========================================================

// Entidad que representa la tabla especializacion.
import com.uniminuto.clinica.entity.Especializacion;

// Repositorio encargado de consultar MySQL.
import com.uniminuto.clinica.repository.EspecializacionRepository;

// Interfaz que define las operaciones del servicio.
import com.uniminuto.clinica.service.EspecializacionService;

// Permite trabajar con listas de registros.
import java.util.List;

// Identifica esta clase como un servicio de Spring Boot.
import org.springframework.stereotype.Service;

/**
 * ==========================================================
 * SERVICIO DE GESTIÓN DE ESPECIALIZACIONES
 * ==========================================================
 *
 * Esta clase implementa los métodos definidos
 * en EspecializacionService.java.
 *
 * Su función es consultar las especializaciones
 * almacenadas en la base de datos clinica.
 *
 * Tabla: especializacion
 *
 * Campos:
 * - id
 * - nombre
 * - descripcion
 * - codigo_especializacion
 */
@Service
public class EspecializacionServiceImpl
        implements EspecializacionService {

    // ======================================================
    // 1. REPOSITORIO DE ESPECIALIZACIONES
    // ======================================================

    /**
     * Permite acceder a los registros almacenados
     * en la tabla especializacion de MySQL.
     */
    private final EspecializacionRepository especializacionRepository;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot inyecta automáticamente
     * el repositorio de especializaciones.
     *
     * @param especializacionRepository
     * Repositorio utilizado para consultar MySQL.
     */
    public EspecializacionServiceImpl(
            EspecializacionRepository especializacionRepository) {

        this.especializacionRepository = especializacionRepository;

    }

    // ======================================================
    // 3. MÉTODO: LISTAR ESPECIALIZACIONES
    // ======================================================

    /**
     * Consulta todas las especializaciones registradas.
     *
     * Funcionamiento:
     *
     * 1. Recibe la solicitud desde el controlador.
     * 2. Utiliza EspecializacionRepository.
     * 3. Consulta los datos almacenados en MySQL.
     * 4. Ordena los registros por nombre.
     * 5. Devuelve la lista de especializaciones.
     *
     * Ejemplos:
     * - Anestesiología
     * - Cardiología
     * - Dermatología
     * - Neurología
     *
     * @return Lista de especializaciones ordenadas.
     */
    @Override
    public List<Especializacion> listarEspecializaciones() {

        // Consultar todas las especializaciones
        // y ordenarlas alfabéticamente por nombre.
        return especializacionRepository
                .findAllByOrderByNombreAsc();

    }

}
