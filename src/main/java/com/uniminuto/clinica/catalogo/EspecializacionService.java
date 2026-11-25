package com.uniminuto.clinica.catalogo;

import org.springframework.stereotype.Service;

import java.util.List;

/** Consulta del catálogo de especialidades (su gestión, activación y tarifas están en el módulo de tarifario). */
@Service
public class EspecializacionService {

    /** Acceso a datos de especialidades. */
    private final EspecializacionRepository repositorio;

    /**
     * @param repositorio repositorio de especialidades.
     */
    public EspecializacionService(EspecializacionRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** @return especialidades por nombre. */
    public List<EspecializacionDto> listar() {
        return repositorio.listar();
    }
}
