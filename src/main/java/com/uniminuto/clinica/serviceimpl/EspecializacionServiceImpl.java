package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Override
    public List<Especializacion> listarEspecializaciones() {
        return especializacionRepository.findAll().stream()
                .sorted((e1, e2) -> e1.getNombre()
                        .compareToIgnoreCase(e2.getNombre())).toList();
    }

    @Override
    public MiRespuestaRS crearEspecializacion(Especializacion especializacion) throws BadRequestException {
        this.validarEspecializacion(especializacion);
        Optional<Especializacion> especializacionExistente = especializacionRepository
                .findByCodigoEspecializacion(especializacion.getCodigoEspecializacion());
        if (especializacionExistente.isPresent()) {
            throw new BadRequestException("La especialización con el código proporcionado ya existe.");
        }

        Optional<Especializacion> especializacionPorNombre = especializacionRepository
                .findByNombre(especializacion.getNombre());
        if (especializacionPorNombre.isPresent()) {
            throw new BadRequestException("La especialización con el nombre proporcionado ya existe.");
        }

        especializacionRepository.save(especializacion);
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Especialización creada exitosamente.");
        rta.setStatus(200);
        return rta;
    }

    private void validarEspecializacion(Especializacion especializacion) throws BadRequestException {
        if (especializacion.getCodigoEspecializacion() == null || especializacion.getCodigoEspecializacion().isEmpty()) {
            throw new BadRequestException("El código de la especialización no puede estar vacío.");
        }
        if (especializacion.getNombre() == null || especializacion.getNombre().isEmpty()) {
            throw new BadRequestException("El nombre de la especialización no puede estar vacío.");
        }
        if (especializacion.getDescripcion() == null || especializacion.getDescripcion().isEmpty()) {
            throw new BadRequestException("La descripción de la especialización no puede estar vacía.");
        }
    }

    @Override
    public MiRespuestaRS actualizarEspecializacion(Especializacion especializacion) throws BadRequestException {
        this.validarEspecializacion(especializacion);
        Optional<Especializacion> especializacionExistente = especializacionRepository
                .findById(especializacion.getId());
        if (especializacionExistente.isEmpty()) {
            throw new BadRequestException("La especialización que se intenta actualizar no existe.");
        }
        Especializacion especializacionActual = especializacionExistente.get();
        if (especializacionActual.getCodigoEspecializacion().equals(especializacion.getCodigoEspecializacion())) {
            Optional<Especializacion> especializacionPorCodigo = especializacionRepository
                    .findByCodigoEspecializacion(especializacion.getCodigoEspecializacion());
            if (especializacionPorCodigo.isPresent() && !especializacionPorCodigo.get().getId()
                    .equals(especializacion.getId())) {
                throw new BadRequestException("La especialización con el código proporcionado ya existe.");
            }
        }

        if (especializacionActual.getNombre().equals(especializacion.getNombre())) {
            Optional<Especializacion> especializacionPorNombre = especializacionRepository
                    .findByNombre(especializacion.getNombre());
            if (especializacionPorNombre.isPresent() && !especializacionPorNombre.get().getId()
                    .equals(especializacion.getId())) {
                throw new BadRequestException("La especialización con el nombre proporcionado ya existe.");
            }
        }

        especializacionActual.setCodigoEspecializacion(especializacion.getCodigoEspecializacion());
        especializacionActual.setNombre(especializacion.getNombre());
        especializacionActual.setDescripcion(especializacion.getDescripcion());
        especializacionRepository.save(especializacionActual);
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Especialización actualizada exitosamente.");
        rta.setStatus(200);
        return rta;
    }
}
