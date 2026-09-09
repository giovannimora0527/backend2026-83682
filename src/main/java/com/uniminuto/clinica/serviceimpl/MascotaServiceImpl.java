package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.MascotaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class MascotaServiceImpl implements MascotaService {

   @Autowired
   private MascotaRepository mascotaRepository;

   @Autowired
   private RazaRepository razaRepository;

   @Autowired
   private ClienteRepository clienteRepository;

    @Override
    public List<Mascota> listarMascotas() throws BadRequestException {
        return mascotaRepository.findAll();
    }

    @Override
    public List<Mascota> listarMascotasOrdenado(boolean ascendente) throws BadRequestException {
        List<Mascota> mascotas = mascotaRepository.findAll();
        if (ascendente) {
            mascotas.sort(
                    Comparator.comparing(Mascota::getNombreMascota)
            );
        } else {
            mascotas.sort(
                    Comparator.comparing(Mascota::getNombreMascota)
                            .reversed()
            );
        }
        return mascotas;
    }

    @Override
    public List<Mascota> listarMascotasPorRaza(Integer idRaza) throws BadRequestException {
        // Paso 1. Valido que idRaza no sea nulo o negativo
        if (idRaza == null || idRaza <= 0) {
            throw new BadRequestException("El idRaza no puede ser nulo o negativo");
        }

        // Paso 2. Consulto la raza dado el id de la raza
        Optional<Raza> optRaza = this.razaRepository.findById(idRaza);
        if (optRaza.isEmpty()){
            throw new BadRequestException("No existe la raza con id: " + idRaza);
        }

        // Consulto las mascotas por raza y devuelvo el resultado
        return this.mascotaRepository.findAllByRaza(optRaza.get());

    }

    @Override
    public List<Mascota> listarMascotasPorCliente(Long idCliente) throws BadRequestException {
        //Paso 1. Valido que idCliente no sea nulo o negativo
        if (idCliente == null || idCliente <= 0) {
            throw new BadRequestException("El idCliente no puede ser nulo o negativo");
        }

        // Paso 2. Consulto el cliente dado el id del cliente
        Optional<Cliente> optCliente = this.clienteRepository.findById(idCliente);
        if (optCliente.isEmpty()){
            throw new BadRequestException("No existe el cliente con id: " + idCliente);
        }

        // Consulto las mascotas por cliente y devuelvo el resultado
        return this.mascotaRepository.findAllByCliente(optCliente.get());
    }

    @Override
    public MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException {
        //Paso 1. Validar el objeto de entrada
        this.validarMascotaRq(mascotaRq);
        // Paso 2. Consulto si la raza existe
        Optional<Raza> optRaza = this.razaRepository.findById(mascotaRq.getRazaId());
        if  (optRaza.isEmpty()) {
            throw new BadRequestException("La raza seleccionada no es valida");
        }

        Optional<Cliente> optCliente = this.clienteRepository.findById(mascotaRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente seleccionado no es valido");
        }

        // Paso 3. Creo la mascota y la guardo en la base de datos
        Mascota mascota = new Mascota();
        mascota.setNombreMascota(mascotaRq.getNombreMascota());
        mascota.setEdad(mascotaRq.getEdad());
        mascota.setRaza(optRaza.get());
        mascota.setCliente(optCliente.get());
        mascota.setFechaRegistro(LocalDateTime.now());

        this.mascotaRepository.save(mascota);

        // Paso 4. Creo la respuesta y la devuelvo
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage("Mascota guardada exitosamente");
        respuesta.setStatus(200);

        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException {
        //Paso 1. Validar el objeto de entrada
        this.validarMascotaRq(mascotaRq);

        // Paso 2. Consulto si la raza existe
        Optional<Raza> optRaza = this.razaRepository.findById(mascotaRq.getRazaId());
        if  (optRaza.isEmpty()) {
            throw new BadRequestException("La raza seleccionada no es valida");
        }

        Optional<Cliente> optCliente = this.clienteRepository.findById(mascotaRq.getClienteId());
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente seleccionado no es valido");
        }

        // Paso 3. Consulto si la mascota existe
        Optional<Mascota> optMascota = this.mascotaRepository.findById(mascotaRq.getMascotaId());
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota seleccionada no es valida");
        }

        // Paso 4. Actualizar los datos
        Mascota mascotaExistente = optMascota.get();
        mascotaExistente.setNombreMascota(mascotaRq.getNombreMascota());
        mascotaExistente.setEdad(mascotaRq.getEdad());
        mascotaExistente.setRaza(optRaza.get());
        mascotaExistente.setCliente(optCliente.get());
        mascotaExistente.setFechaRegistro(LocalDateTime.now());

        this.mascotaRepository.save(mascotaExistente);

        // Paso 5. Creo la respuesta y la devuelvo
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage("Mascota actualizada exitosamente");
        respuesta.setStatus(200);

        return respuesta;
    }

    private void validarMascotaRq(MascotaRq mascotaRq) throws BadRequestException {
        if (mascotaRq == null) {
            throw new BadRequestException("El objeto MascotaRq no puede ser nulo");
        }
        if (mascotaRq.getNombreMascota() == null || mascotaRq.getNombreMascota().isEmpty()) {
            throw new BadRequestException("El nombre de la mascota no puede ser nulo o vacío");
        }
        if (mascotaRq.getEdad() == null || mascotaRq.getEdad() <= 0) {
            throw new BadRequestException("La edad de la mascota no puede ser nula o negativa");
        }
        if (mascotaRq.getRazaId() == null || mascotaRq.getRazaId() <= 0) {
            throw new BadRequestException("El id de la raza no puede ser nulo o negativo");
        }
        if (mascotaRq.getClienteId() == null || mascotaRq.getClienteId() <= 0) {
            throw new BadRequestException("El id del cliente no puede ser nulo o negativo");
        }
    }
}
