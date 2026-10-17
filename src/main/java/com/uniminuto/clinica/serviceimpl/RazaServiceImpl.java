package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Razas.
 */
@Service
public class RazaServiceImpl implements RazaService {

    @Autowired
    private RazaRepository razaRepository;

    /**
     * Crea una nueva raza en el sistema validando que no exista previamente.
     * @param razaRq Objeto con los datos de la raza a crear.
     * @return Respuesta estandarizada de éxito.
     * @throws BadRequestException si los campos están vacíos o la raza ya existe.
     */
    @Override
    public MiRespuestaRS crearRazaNueva(RazaRq razaRq) throws BadRequestException {
        this.validarRazaRq(razaRq);
        Optional<Raza> optRaza = this.razaRepository.findByEspecieAndNombre(razaRq.getEspecie(), razaRq.getNombre());

        if (optRaza.isPresent()) {
            throw new BadRequestException("La raza ya existe para la especie especificada.");
        }

        Raza nuevaRaza = new Raza();
        nuevaRaza.setEspecie(razaRq.getEspecie());
        nuevaRaza.setNombre(razaRq.getNombre());
        nuevaRaza.setFechaCreacion(LocalDateTime.now());
        this.razaRepository.save(nuevaRaza);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza creada correctamente");
        return respuesta;
    }

    /**
     * Lista todas las razas ordenadas alfabéticamente por su nombre.
     * @return Lista de razas.
     * @throws BadRequestException si ocurre un error.
     */
    @Override
    public List<Raza> listarRazas() throws BadRequestException {
        return razaRepository.findAll().stream()
                .sorted((r1, r2) -> r1.getNombre().compareToIgnoreCase(r2.getNombre()))
                .toList();
    }

    private void validarRazaRq(RazaRq razaRq) throws BadRequestException {
        if (razaRq.getNombre() == null || razaRq.getNombre().isEmpty()) {
            throw new BadRequestException("El nombre de la raza es obligatorio.");
        }
        if (razaRq.getEspecie() == null || razaRq.getEspecie().isEmpty()) {
            throw new BadRequestException("La especie de la raza es obligatoria.");
        }
    }
}