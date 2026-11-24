package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.models.FormulaRs;
import com.uniminuto.clinica.repository.FormulaMedicaDetalleRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.security.UsuarioAutenticado;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FormulaServiceImpl implements FormulaService {

    private final FormulaMedicaRepository formulaRepository;
    private final FormulaMedicaDetalleRepository detalleRepository;
    private final UsuarioActual usuarioActual;

    public FormulaServiceImpl(FormulaMedicaRepository formulaRepository,
                              FormulaMedicaDetalleRepository detalleRepository, UsuarioActual usuarioActual) {
        this.formulaRepository = formulaRepository;
        this.detalleRepository = detalleRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public List<FormulaRs> listar() {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        return convertir(usuario.esMedico()
                ? formulaRepository.findByMedico_IdOrderByFechaGeneracionDesc(usuario.medicoId())
                : formulaRepository.findAllByOrderByFechaGeneracionDesc());
    }

    @Override
    public List<FormulaRs> listarPorCitas(List<Long> citaIds) {
        return convertir(formulaRepository.findByCita_IdInOrderByFechaGeneracionDesc(citaIds));
    }

    private List<FormulaRs> convertir(List<FormulaMedica> formulas) {
        Map<Long, List<FormulaMedicaDetalle>> detalles = detalleRepository
                .findByFormula_IdInOrderById(formulas.stream().map(FormulaMedica::getId).toList()).stream()
                .collect(Collectors.groupingBy(detalle -> detalle.getFormula().getId()));
        return formulas.stream()
                .map(formula -> FormulaRs.de(formula, detalles.getOrDefault(formula.getId(), List.of())))
                .toList();
    }
}
