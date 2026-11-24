package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface EspecializacionService {

    List<Especializacion> listarEspecializaciones(boolean soloActivas);

    MiRespuestaRS crearEspecializacion(Especializacion especializacion)
            throws BadRequestException;

    MiRespuestaRS actualizarEspecializacion(Especializacion especializacion)
            throws BadRequestException;

    Especializacion cambiarEstado(Long id, boolean activo);
}
