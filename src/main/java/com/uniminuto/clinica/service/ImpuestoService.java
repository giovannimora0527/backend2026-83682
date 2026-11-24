package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.ImpuestoRq;
import com.uniminuto.clinica.models.ImpuestoRs;
import java.util.List;

public interface ImpuestoService {

    List<ImpuestoRs> listar();

    ImpuestoRs crear(ImpuestoRq rq);

    ImpuestoRs actualizar(Long id, ImpuestoRq rq);
}
