
package com.uniminuto.clinica.apicontroller;

// Interfaz que define las rutas de la API.
import com.uniminuto.clinica.api.MascotaApi;
import com.uniminuto.clinica.dto.CrearMascotaDTO;
// Entidad que representa la tabla mascota de MySQL.
import com.uniminuto.clinica.entity.Mascota;

// DTO que contiene los datos de mascota, raza y propietario.
import com.uniminuto.clinica.dto.MascotaDetalleDTO;

// Servicio encargado de consultar las mascotas.
import com.uniminuto.clinica.service.MascotaService;

// Manejo de excepciones.
import org.apache.coyote.BadRequestException;

// Inyección de dependencias.
import org.springframework.beans.factory.annotation.Autowired;

// Permite devolver respuestas HTTP.
import org.springframework.http.ResponseEntity;

// Indica que esta clase es un controlador REST.
import org.springframework.web.bind.annotation.RestController;

// Manejo de listas.
import java.util.List;

/**
 * CONTROLADOR DE MASCOTAS
 *
 * Recibe las peticiones HTTP desde Angular
 * y utiliza MascotaService para consultar
 * la información almacenada en MySQL.
 *
 * Implementa las rutas declaradas en MascotaApi.
 */
@RestController
public class nMascotaApiController implements MascotaApi {

    /**
     * Inyecta el servicio de mascotas.
     *
     * Spring Boot proporciona automáticamente
     * una instancia de MascotaServiceImpl.
     */
    @Autowired
    private MascotaService mascotaService;

    /**
     * MÉTODO 1: LISTAR TODAS LAS MASCOTAS
     *
     * Endpoint:
     * GET /mascota/listar-mascotas
     *
     * Devuelve las mascotas registradas.
     */
    @Override
    public ResponseEntity<List<Mascota>> listarMascotas()
            throws BadRequestException {

        // Consultar las mascotas utilizando el servicio.
        List<Mascota> mascotas = mascotaService.listarMascotas();

        // Devolver HTTP 200 con la lista de mascotas.
        return ResponseEntity.ok(mascotas);
    }

    /**
     * MÉTODO 2: LISTAR MASCOTAS ORDENADAS
     *
     * Endpoint:
     * GET /mascota/listar-mascotas-ordenado
     *
     * ascendente = true: orden A-Z.
     * ascendente = false: orden Z-A.
     */
    @Override
    public ResponseEntity<List<Mascota>> listarMascotas(
            boolean ascendente) throws BadRequestException {

        // Consultar las mascotas ordenadas.
        List<Mascota> mascotas =
                mascotaService.listarMascotasOrdenado(ascendente);

        // Devolver HTTP 200 con los resultados.
        return ResponseEntity.ok(mascotas);
    }

    /**
     * MÉTODO 3: LISTAR MASCOTAS CON DETALLES
     *
     * Endpoint:
     * GET /mascota/listar-detalle
     *
     * Devuelve:
     * - Identificador de mascota.
     * - Nombre de mascota.
     * - Edad.
     * - Fecha de registro.
     * - Nombre y especie de raza.
     * - Nombre y apellidos del propietario.
     *
     * Utiliza MascotaDetalleDTO para organizar
     * la información que recibirá Angular.
     */
    @Override
    public ResponseEntity<List<MascotaDetalleDTO>> listarDetalle()
            throws BadRequestException {

        // Obtener las mascotas con sus datos relacionados.
        List<MascotaDetalleDTO> mascotas =
                mascotaService.listarDetalle();

        // Devolver la información en formato JSON.
        return ResponseEntity.ok(mascotas);
    }


    /**
     * Recibe los datos desde Angular y solicita
     * al servicio registrar la mascota en MySQL.
     */
    @Override
    public ResponseEntity<Mascota> crearMascota(
            CrearMascotaDTO datos) {

        // El servicio valida y guarda la mascota.
        Mascota nuevaMascota =
                mascotaService.crearMascota(datos);

        // Devuelve HTTP 200 con la mascota registrada.
        return ResponseEntity.ok(nuevaMascota);
    }

}
