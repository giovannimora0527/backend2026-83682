
package com.uniminuto.clinica.serviceimpl;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// IMPLEMENTACIÓN DEL SERVICIO DE MÉDICOS
// ==========================================================

// Entidad que representa a los médicos veterinarios.
import com.uniminuto.clinica.entity.Medico;

// Repositorio que consulta la tabla medico en MySQL.
import com.uniminuto.clinica.repository.MedicoRepository;

// Interfaz que define las operaciones del servicio.
import com.uniminuto.clinica.service.MedicoService;

// Anotaciones de Spring Boot.
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Permite devolver una lista de médicos.
import java.util.List;

/**
 * ==========================================================
 * CLASE: MedicoServiceImpl
 * ==========================================================
 *
 * Implementa las operaciones definidas
 * en la interfaz MedicoService.
 *
 * Función principal:
 *
 * Consultar los médicos veterinarios registrados
 * en la base de datos clinica.
 *
 * Los resultados estarán ordenados
 * alfabéticamente por apellidos y nombres.
 *
 * Posteriormente, estos datos se utilizarán
 * en Angular para seleccionar al veterinario
 * al momento de registrar una cita médica.
 */
@Service
public class MedicoServiceImpl implements MedicoService {

    // ======================================================
    // 1. REPOSITORIO DE MÉDICOS
    // ======================================================

    /**
     * Permite acceder a los registros
     * de la tabla medico.
     */
    private final MedicoRepository medicoRepository;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot inyecta automáticamente
     * el repositorio mediante el constructor.
     *
     * @param medicoRepository Repositorio de médicos.
     */
    public MedicoServiceImpl(
            MedicoRepository medicoRepository
    ) {
        this.medicoRepository = medicoRepository;
    }

    // ======================================================
    // 3. LISTAR MÉDICOS VETERINARIOS
    // ======================================================

    /**
     * Obtiene todos los médicos registrados
     * en la tabla medico de MySQL.
     *
     * Orden:
     * 1. Apellidos de A a Z.
     * 2. Nombres de A a Z.
     *
     * @return Lista de médicos veterinarios.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Medico> listarMedicos() {

        // Consultar los médicos usando el repositorio.
        return medicoRepository
                .findAllByOrderByApellidosAscNombresAsc();
    }

}
