package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.MascotaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MascotaServiceImpl implements MascotaService {

   @Autowired
   private MascotaRepository mascotaRepository;

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
}
