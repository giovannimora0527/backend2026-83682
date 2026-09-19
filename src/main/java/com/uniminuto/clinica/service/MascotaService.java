package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface MascotaService {

    List<Mascota> listarMascotas() throws BadRequestException;

    List<Mascota> listarMascotasOrdenado(boolean ascendente) throws BadRequestException;

    List<Mascota> listarMascotasPorRaza(Integer idRaza) throws BadRequestException;

    List<Mascota> listarMascotasPorCliente(Long idCliente) throws BadRequestException;

    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;

}
