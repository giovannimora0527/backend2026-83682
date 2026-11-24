package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.FacturaRs;

import java.util.List;

public interface FacturaService {

    List<FacturaRs> listarPorCliente(Long clienteId);

    List<FacturaRs> listarPorMascota(Integer mascotaId);
}
