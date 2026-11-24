package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.models.PerfilRs;
import com.uniminuto.clinica.repository.PerfilRepository;
import com.uniminuto.clinica.service.PerfilService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository perfilRepository;

    public PerfilServiceImpl(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Override
    public List<PerfilRs> listar() {
        return perfilRepository.findByActivoTrueAndCodigoNotNullOrderByPerfilId().stream()
                .map(p -> new PerfilRs(p.getPerfilId(), p.getCodigo(), p.getNombre(), p.getActivo()))
                .toList();
    }
}
