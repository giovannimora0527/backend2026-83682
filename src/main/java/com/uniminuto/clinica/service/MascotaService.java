package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.Optional;
import java.util.List;

public interface MascotaService {

    List<Mascota> listarMascotas();

    List<Mascota> listarMascotasOrdenado(boolean ascendente);

    Optional<Mascota> findByNombreMascota(String nombreMascota);

    List<Mascota> buscarMascotasPorCliente(Long clienteId) throws BadRequestException;

    List<Mascota> buscarMascotasPorRaza(Integer razaId) throws BadRequestException;

    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;

}

