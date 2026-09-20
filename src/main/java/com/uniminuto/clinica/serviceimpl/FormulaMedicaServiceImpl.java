package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaMedicaRs;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/** Implementacion de {@link FormulaMedicaService}. */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /** Repositorio de formulas medicas. */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /** {@inheritDoc} El orden lo hace la base de datos, no Java. */
    @Override
    public List<FormulaMedicaRs> listarFormulasRecientes() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionDesc()
                .stream().map(this::aRespuesta).toList();
    }

    /** Convierte la entidad en su DTO de respuesta. */
    private FormulaMedicaRs aRespuesta(FormulaMedica f) {
        FormulaMedicaRs r = new FormulaMedicaRs();
        r.setFormulaMedicaId(f.getFormulaMedicaId());
        r.setDescripcion(f.getDescripcion());
        r.setFechaCreacion(f.getFechaCreacion());
        return r;
    }
}
