package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.MascotaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
