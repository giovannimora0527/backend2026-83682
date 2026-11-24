package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.PerfilRs;

import java.util.List;

public interface PerfilService {

    /** Perfiles activos con codigo del contrato. */
    List<PerfilRs> listar();
}
