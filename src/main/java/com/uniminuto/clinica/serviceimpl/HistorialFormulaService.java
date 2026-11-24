package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaHistorialEstado;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.repository.FormulaHistorialEstadoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/** Registra cada cambio de estado de una formula o de su despacho. */
@Component
public class HistorialFormulaService {

    private final FormulaHistorialEstadoRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;

    public HistorialFormulaService(FormulaHistorialEstadoRepository historialRepository,
                                   UsuarioRepository usuarioRepository, UsuarioActual usuarioActual) {
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActual = usuarioActual;
    }

    public void registrar(FormulaMedica formula, EstadoFormula anterior, EstadoDespacho despachoAnterior,
                          String motivo) {
        FormulaHistorialEstado h = new FormulaHistorialEstado();
        h.setFormula(formula);
        h.setEstadoFormulaAnterior(anterior);
        h.setEstadoFormulaNuevo(formula.getEstadoFormula());
        h.setEstadoDespachoAnterior(despachoAnterior);
        h.setEstadoDespachoNuevo(formula.getEstadoDespacho());
        h.setMotivo(motivo);
        h.setUsuario(usuarioRepository.getReferenceById(usuarioActual.obtener().id()));
        h.setFecha(LocalDateTime.now());
        historialRepository.save(h);
    }
}
