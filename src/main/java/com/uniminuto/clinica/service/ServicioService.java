package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.ServicioRq;
import com.uniminuto.clinica.models.ServicioRs;
import java.util.List;

public interface ServicioService {

    List<ServicioRs> listar(boolean soloActivos, Long especializacionId);

    ServicioRs crear(ServicioRq rq);

    ServicioRs actualizar(Long id, ServicioRq rq);

    ServicioRs cambiarEstado(Long id, boolean activo);
}
