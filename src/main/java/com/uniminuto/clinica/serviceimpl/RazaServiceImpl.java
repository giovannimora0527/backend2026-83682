
package com.uniminuto.clinica.serviceimpl;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// IMPLEMENTACIÓN DEL SERVICIO DE RAZAS
// ==========================================================

// Entidad que representa la tabla raza.
import com.uniminuto.clinica.entity.Raza;

// Repositorio que consulta MySQL.
import com.uniminuto.clinica.repository.RazaRepository;

// Interfaz que define los métodos del servicio.
import com.uniminuto.clinica.service.RazaService;

// Permite devolver una lista de razas.
import java.util.List;

// Anotación para registrar el servicio en Spring Boot.
import org.springframework.stereotype.Service;

/**
 * ==========================================================
 * SERVICIO DE GESTIÓN DE RAZAS
 * ==========================================================
 *
 * Implementa las operaciones definidas
 * en la interfaz RazaService.
 *
 * Su función principal es obtener
 * la información de la tabla raza.
 */
@Service
public class RazaServiceImpl implements RazaService {

    // ======================================================
    // 1. REPOSITORIO DE RAZAS
    // ======================================================

    /**
     * Permite acceder a los datos
     * almacenados en MySQL.
     */
    private final RazaRepository razaRepository;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot proporciona automáticamente
     * una instancia de RazaRepository.
     */
    public RazaServiceImpl(RazaRepository razaRepository) {
        this.razaRepository = razaRepository;
    }

    // ======================================================
    // 3. MÉTODO: LISTAR RAZAS
    // ======================================================

    /**
     * Recupera todas las razas registradas
     * y las ordena alfabéticamente.
     *
     * Utiliza el método que definimos:
     * findAllByOrderByNombreAsc()
     *
     * @return Lista de razas.
     */
    @Override
    public List<Raza> listarRazas() {

        // Consultar las razas desde MySQL.
        return razaRepository.findAllByOrderByNombreAsc();

    }

}
