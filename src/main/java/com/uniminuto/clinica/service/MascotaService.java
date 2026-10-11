
package com.uniminuto.clinica.service;

// Entidad que representa la tabla mascota en MySQL.
import com.uniminuto.clinica.entity.Mascota;

// DTO para mostrar la mascota con raza y propietario.
import com.uniminuto.clinica.dto.MascotaDetalleDTO;

// DTO para recibir los datos de una nueva mascota.
import com.uniminuto.clinica.dto.CrearMascotaDTO;

// Manejo de excepciones.
import org.apache.coyote.BadRequestException;

// Permite trabajar con listas de mascotas.
import java.util.List;

/**
 * SERVICIO DE MASCOTAS
 *
 * Esta interfaz define las operaciones
 * disponibles para gestionar mascotas.
 *
 * La lógica de estos métodos se implementa
 * en MascotaServiceImpl.java.
 */
public interface MascotaService {

    /**
     * MÉTODO 1: LISTAR MASCOTAS
     *
     * Obtiene todas las mascotas registradas.
     */
    List<Mascota> listarMascotas()
            throws BadRequestException;

    /**
     * MÉTODO 2: LISTAR MASCOTAS ORDENADAS
     *
     * ascendente = true: orden A-Z.
     * ascendente = false: orden Z-A.
     */
    List<Mascota> listarMascotasOrdenado(
            boolean ascendente
    ) throws BadRequestException;

    /**
     * MÉTODO 3: LISTAR MASCOTAS CON DETALLES
     *
     * Obtiene:
     * - Nombre de la mascota.
     * - Edad.
     * - Raza.
     * - Especie.
     * - Nombre y apellidos del propietario.
     */
    List<MascotaDetalleDTO> listarDetalle()
            throws BadRequestException;

    /**
     * MÉTODO 4: CREAR MASCOTA
     *
     * Recibe un CrearMascotaDTO desde Angular.
     *
     * MascotaServiceImpl deberá:
     * 1. Validar el nombre y la edad.
     * 2. Verificar la raza y el propietario.
     * 3. Asignar la fecha de registro.
     * 4. Guardar la mascota en MySQL.
     *
     * Devuelve la mascota registrada.
     */
    Mascota crearMascota(CrearMascotaDTO datos);
}
