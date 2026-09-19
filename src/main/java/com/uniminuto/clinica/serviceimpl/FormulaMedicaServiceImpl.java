package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.FormulaMedicaDTO;
import com.uniminuto.clinica.models.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacion de {@link FormulaMedicaService}.
 *
 * <p>Resuelve el Requerimiento 1 del taller: listar las formulas medicas
 * del inventario ordenadas por fecha de creacion, de la mas reciente a la
 * mas antigua.</p>
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /** Repositorio para acceder a los datos de FormulaMedica en la base de datos. */
    private final FormulaMedicaRepository formulaMedicaRepository;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param formulaMedicaRepository repositorio de formulas medicas
     */
    public FormulaMedicaServiceImpl(FormulaMedicaRepository formulaMedicaRepository) {
        this.formulaMedicaRepository = formulaMedicaRepository;
    }

    /** {@inheritDoc} */
    @Override
    public List<FormulaMedicaDTO> listarFormulasMedicas() {
        // El propio repositorio ya se encarga de traer los datos ordenados
        // de la mas reciente a la mas antigua (ver FormulaMedicaRepository).
        return formulaMedicaRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad {@link FormulaMedica} en su correspondiente
     * {@link FormulaMedicaDTO}.
     *
     * @param formula entidad a convertir
     * @return DTO con los datos de la formula medica
     */
    private FormulaMedicaDTO convertirADTO(FormulaMedica formula) {
        FormulaMedicaDTO dto = new FormulaMedicaDTO();
        dto.setId(formula.getId());
        dto.setFechaCreacion(formula.getFechaCreacion());
        dto.setMedicamento(formula.getMedicamento());
        dto.setDosis(formula.getDosis());
        dto.setIndicaciones(formula.getIndicaciones());
        dto.setCantidad(formula.getCantidad());
        if (formula.getPaciente() != null) {
            dto.setNombrePaciente(formula.getPaciente().getNombre());
        }
        return dto;
    }
}
