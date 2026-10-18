
package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

/**
 * Define las operaciones de consulta, registro y actualización de mascotas.
 */
public interface MascotaService {

    /**
     * Obtiene todas las mascotas registradas.
     *
     * @return lista completa de mascotas
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    List<Mascota> listarMascotas() throws BadRequestException;

    /**
     * Obtiene las mascotas ordenadas por nombre.
     *
     * @param ascendente indica si el orden será ascendente o descendente
     * @return lista de mascotas ordenadas por nombre
     * @throws BadRequestException si ocurre un error en la solicitud
     */
    List<Mascota> listarMascotasOrdenado(boolean ascendente) throws BadRequestException;

    /**
     * Consulta las mascotas asociadas a una raza.
     *
     * @param idRaza identificador de la raza
     * @return lista de mascotas asociadas a la raza
     * @throws BadRequestException si el identificador no es válido o la raza no existe
     */
    List<Mascota> listarMascotasPorRaza(Integer idRaza) throws BadRequestException;

    /**
     * Consulta las mascotas asociadas a un cliente.
     *
     * @param idCliente identificador del cliente propietario
     * @return lista de mascotas del cliente
     * @throws BadRequestException si el identificador no es válido o el cliente no existe
     */
    List<Mascota> listarMascotasPorCliente(Long idCliente) throws BadRequestException;

    /**
     * Registra una nueva mascota después de validar sus datos.
     *
     * @param mascotaRq información de la mascota que se va a registrar
     * @return respuesta con el estado y el mensaje de la operación
     * @throws BadRequestException si los datos o sus referencias no son válidos
     */
    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    /**
     * Actualiza una mascota existente después de validar sus datos.
     *
     * @param mascotaRq información actualizada de la mascota
     * @return respuesta con el estado y el mensaje de la operación
     * @throws BadRequestException si los datos, el identificador o sus referencias no son válidos
     */
    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;

    /**
     * Consulta una mascota por su identificador.
     *
     * @param id identificador de la mascota
     * @return mascota encontrada
     */
    Mascota consultarMascota(Integer id);

    /**
     * Busca una mascota por su nombre (servicio del parcial).
     *
     * @param nombre nombre de la mascota
     * @return mascota encontrada
     */
    Mascota buscarMascotaPorNombre(String nombre) throws BadRequestException;

    /**
     * Elimina una mascota que no tenga citas ni historias clínicas asociadas.
     *
     * @param mascotaId identificador de la mascota
     * @return respuesta con el resultado
     */
    MiRespuestaRS eliminarMascota(Integer mascotaId) throws BadRequestException;
}
