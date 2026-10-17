package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Mascotas.
 */
@Service
public class MascotaServiceImpl implements MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RazaRepository razaRepository;

    /**
     * Lista todas las mascotas ordenadas alfabéticamente por su nombre.
     * @return Lista de mascotas.
     */
    @Override
    public List<Mascota> listarMascotas() {
        List<Mascota> mascotas = mascotaRepository.findAll();
        mascotas.sort((m1, m2) -> m1.getNombreMascota().compareToIgnoreCase(m2.getNombreMascota()));
        return mascotas;
    }

    /**
     * Lista todas las mascotas ordenadas por nombre de forma ascendente o descendente.
     * @param ascendente Indica el tipo de orden.
     * @return Lista de mascotas ordenadas.
     */
    @Override
    public List<Mascota> listarMascotasOrdenado(boolean ascendente) {
        if (ascendente) {
            return mascotaRepository.findAllByOrderByNombreMascotaAsc();
        } else {
            return mascotaRepository.findAllByOrderByNombreMascotaDesc();
        }
    }

    /**
     * Busca una mascota por su nombre.
     * @param nombreMascota Nombre de la mascota.
     * @return Optional con la mascota encontrada.
     */
    @Override
    public Optional<Mascota> findByNombreMascota(String nombreMascota) {
        return Optional.ofNullable(mascotaRepository.findByNombreMascota(nombreMascota));
    }

    /**
     * Busca todas las mascotas pertenecientes a un cliente específico.
     * @param clienteId ID del cliente.
     * @return Lista de mascotas del cliente.
     * @throws BadRequestException si el ID es nulo o el cliente no existe.
     */
    @Override
    public List<Mascota> buscarMascotasPorCliente(Long clienteId) throws BadRequestException {
        if (clienteId == null) {
            throw new BadRequestException("El ID del cliente no puede ser nulo");
        }
        Optional<Cliente> optCliente = this.clienteRepository.findById(clienteId);
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + clienteId + " no existe");
        }
        return this.mascotaRepository.findByClienteOrderByNombreMascotaAsc(optCliente.get());
    }

    /**
     * Busca todas las mascotas de una raza específica.
     * @param razaId ID de la raza.
     * @return Lista de mascotas de la raza.
     * @throws BadRequestException si el ID es nulo o la raza no existe.
     */
    @Override
    public List<Mascota> buscarMascotasPorRaza(Integer razaId) throws BadRequestException {
        if (razaId == null) {
            throw new BadRequestException("El ID de la raza no puede ser nulo");
        }
        Optional<Raza> optRaza = this.razaRepository.findById(razaId);
        if (optRaza.isEmpty()) {
            throw new BadRequestException("La raza con ID " + razaId + " no existe");
        }
        return this.mascotaRepository.findByRazaOrderByNombreMascotaAsc(optRaza.get());
    }

    /**
     * Guarda una nueva mascota en la base de datos.
     * @param mascotaRq Objeto con los datos de la mascota a guardar.
     * @return Respuesta estandarizada de éxito.
     * @throws BadRequestException si los datos son inválidos o las relaciones no existen.
     */
    @Override
    public MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException {
        try {
            this.validarMascotaRq(mascotaRq);
        } catch (BadRequestException e) {
            throw new BadRequestException("Error al validar la mascota: " + e.getMessage());
        }

        Optional<Raza> optRaza = this.razaRepository.findById(mascotaRq.getRazaId());
        if (optRaza.isEmpty()) {
            throw new BadRequestException("La raza con ID " + mascotaRq.getRazaId() + " no existe");
        }

        Optional<Cliente> optCliente = this.clienteRepository.findById(mascotaRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + mascotaRq.getClienteId() + " no existe");
        }

        Mascota nueva = new Mascota();
        nueva.setNombreMascota(mascotaRq.getNombre());
        nueva.setEdad(mascotaRq.getEdad());
        nueva.setCliente(optCliente.get());
        nueva.setRaza(optRaza.get());
        nueva.setFechaRegistro(LocalDateTime.now());

        this.mascotaRepository.save(nueva);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Mascota guardada correctamente");
        return respuesta;
    }

    private void validarMascotaRq(MascotaRq mascotaRq) throws BadRequestException {
        if (mascotaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede ser nulo");
        }
        if (mascotaRq.getNombre() == null || mascotaRq.getNombre().isEmpty()) {
            throw new BadRequestException("El nombre de la mascota no puede ser nulo o vacío");
        }
        if (mascotaRq.getClienteId() == null) {
            throw new BadRequestException("El id del cliente de entrada no es válido");
        }
        if (mascotaRq.getRazaId() == null) {
            throw new BadRequestException("El id de la raza de entrada no es válido");
        }
    }

    /**
     * Actualiza los datos de una mascota existente.
     * @param mascotaRq Objeto con los datos actualizados y el ID de la mascota.
     * @return Respuesta estandarizada de éxito.
     * @throws BadRequestException si la mascota, cliente o raza no existen.
     */
    @Override
    public MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException {
        try {
            this.validarMascotaRq(mascotaRq);
        } catch (BadRequestException e) {
            throw new BadRequestException("Error al validar la mascota: " + e.getMessage());
        }

        Optional<Raza> optRaza = this.razaRepository.findById(mascotaRq.getRazaId());
        if (optRaza.isEmpty()) {
            throw new BadRequestException("La raza con ID " + mascotaRq.getRazaId() + " no existe");
        }

        Optional<Cliente> optCliente = this.clienteRepository.findById(mascotaRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + mascotaRq.getClienteId() + " no existe");
        }

        Optional<Mascota> optMascota = this.mascotaRepository.findById(mascotaRq.getMascotaId());
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + mascotaRq.getMascotaId() + " no existe");
        }

        Mascota existente = optMascota.get();
        existente.setNombreMascota(mascotaRq.getNombre());
        existente.setEdad(mascotaRq.getEdad());
        existente.setCliente(optCliente.get());
        existente.setRaza(optRaza.get());
        existente.setFechaModificacion(LocalDateTime.now());

        this.mascotaRepository.save(existente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Mascota actualizada correctamente");
        return respuesta;
    }
}