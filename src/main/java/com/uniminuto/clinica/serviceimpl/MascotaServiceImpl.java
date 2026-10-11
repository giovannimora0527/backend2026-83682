
package com.uniminuto.clinica.serviceimpl;

// DTO para registrar mascotas.
import com.uniminuto.clinica.dto.CrearMascotaDTO;

// DTO para mostrar mascota, raza y propietario.
import com.uniminuto.clinica.dto.MascotaDetalleDTO;

// Entidad de la tabla mascota.
import com.uniminuto.clinica.entity.Mascota;

// Repositorios para consultar MySQL.
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;

// Interfaz del servicio.
import com.uniminuto.clinica.service.MascotaService;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementación del servicio de mascotas.
 *
 * Contiene la lógica para consultar y registrar
 * mascotas en la base de datos MySQL.
 */
@Service
public class MascotaServiceImpl implements MascotaService {

    // Repositorio para consultar los propietarios.
    @Autowired
    private ClienteRepository clienteRepository;

    // Repositorio para consultar y guardar mascotas.
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * MÉTODO 1: Consultar todas las mascotas.
     */
    @Override
    public List<Mascota> listarMascotas()
            throws BadRequestException {

        return mascotaRepository.findAll();
    }

    /**
     * MÉTODO 2: Listar mascotas ordenadas.
     *
     * ascendente = true: A-Z.
     * ascendente = false: Z-A.
     */
    @Override
    public List<Mascota> listarMascotasOrdenado(
            boolean ascendente) throws BadRequestException {

        List<Mascota> mascotas = mascotaRepository.findAll();

        Comparator<Mascota> comparador =
                Comparator.comparing(
                        Mascota::getNombreMascota,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                );

        if (!ascendente) {
            comparador = comparador.reversed();
        }

        mascotas.sort(comparador);

        return mascotas;
    }

    /**
     * MÉTODO 3: Consultar mascotas con detalles.
     *
     * Obtiene los datos de:
     * - Mascota.
     * - Raza.
     * - Especie.
     * - Propietario.
     *
     * Convierte los resultados SQL a DTO.
     */
    @Override
    public List<MascotaDetalleDTO> listarDetalle()
            throws BadRequestException {

        // Ejecutar consulta SQL con las tres tablas.
        List<Object[]> filas =
                mascotaRepository.listarMascotasConDetalle();

        // Crear lista de resultados.
        List<MascotaDetalleDTO> resultado = new ArrayList<>();

        // Recorrer cada registro.
        for (Object[] fila : filas) {

            // Obtener información de la raza.
            MascotaDetalleDTO.RazaDetalle raza =
                    fila[4] == null
                            ? null
                            : new MascotaDetalleDTO.RazaDetalle(
                            ((Number) fila[4]).intValue(),
                            (String) fila[5],
                            (String) fila[6]
                    );

            // Obtener información del propietario.
            MascotaDetalleDTO.ClienteDetalle cliente =
                    fila[7] == null
                            ? null
                            : new MascotaDetalleDTO.ClienteDetalle(
                            ((Number) fila[7]).longValue(),
                            (String) fila[8],
                            (String) fila[9]
                    );

            // Obtener fecha de registro.
            String fechaRegistro =
                    fila[3] == null
                            ? null
                            : fila[3].toString();

            // Construir objeto con todos los datos.
            MascotaDetalleDTO mascota =
                    new MascotaDetalleDTO(
                            ((Number) fila[0]).intValue(),
                            (String) fila[1],
                            ((Number) fila[2]).intValue(),
                            fechaRegistro,
                            raza,
                            cliente
                    );

            resultado.add(mascota);
        }

        // Devolver lista completa.
        return resultado;
    }

    /**
     * MÉTODO 4: Crear una nueva mascota.
     *
     * Recibe:
     * - Nombre de mascota.
     * - Edad.
     * - Identificador de raza.
     * - Identificador del propietario.
     *
     * Valida los datos antes de guardarlos.
     */
    @Override
    @Transactional
    public Mascota crearMascota(CrearMascotaDTO datos) {

        // 1. Comprobar que se enviaron los datos.
        if (datos == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe enviar los datos de la mascota"
            );
        }

        // 2. Validar el nombre.
        if (datos.nombreMascota() == null ||
                datos.nombreMascota().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre de la mascota es obligatorio"
            );
        }

        String nombre = datos.nombreMascota().trim();

        // Validar longitud máxima.
        if (nombre.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar 100 caracteres"
            );
        }

        // 3. Validar edad.
        if (datos.edad() == null || datos.edad() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La edad debe ser igual o mayor que cero"
            );
        }

        // 4. Validar el ID de raza.
        if (datos.razaId() == null || datos.razaId() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar una raza válida"
            );
        }

        // Comprobar que exista la raza en MySQL.
        if (mascotaRepository.contarRazaPorId(
                datos.razaId()) == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La raza seleccionada no existe"
            );
        }

        // 5. Validar el ID del propietario.
        if (datos.clienteId() == null ||
                datos.clienteId() < 1 ||
                datos.clienteId() > Integer.MAX_VALUE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador del propietario no es válido"
            );
        }

        // Comprobar que exista el propietario.
        if (!clienteRepository.existsById(
                datos.clienteId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El propietario seleccionado no existe"
            );
        }

        // 6. Crear la nueva mascota.
        Mascota mascota = new Mascota();

        // Asignar los valores recibidos del formulario.
        mascota.setNombreMascota(nombre);
        mascota.setEdad(datos.edad());
        mascota.setRazaId(datos.razaId());
        mascota.setClienteId(
                datos.clienteId().intValue()
        );

        // 7. Registrar la fecha actual.
        mascota.setFechaRegistro(LocalDateTime.now());

        // 8. Guardar mascota en MySQL.
        return mascotaRepository.save(mascota);
    }
}
