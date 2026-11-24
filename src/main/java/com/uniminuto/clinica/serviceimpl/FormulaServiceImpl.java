package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaHistorialEstado;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.FormulaRs;
import com.uniminuto.clinica.pdf.FormulaPdf;
import com.uniminuto.clinica.repository.AtencionMedicaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.DespachoRepository;
import com.uniminuto.clinica.repository.FormulaHistorialEstadoRepository;
import com.uniminuto.clinica.repository.FormulaMedicaDetalleRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.Rol;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.security.UsuarioAutenticado;
import com.uniminuto.clinica.service.ConfiguracionService;
import com.uniminuto.clinica.service.FormulaService;
import com.uniminuto.clinica.util.RangoFechas;
import com.uniminuto.clinica.util.Texto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FormulaServiceImpl implements FormulaService {

    private final FormulaMedicaRepository formulaRepository;
    private final FormulaMedicaDetalleRepository detalleRepository;
    private final FormulaHistorialEstadoRepository historialRepository;
    private final DespachoRepository despachoRepository;
    private final CitaRepository citaRepository;
    private final AtencionMedicaRepository atencionRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionService configuracion;
    private final UsuarioActual usuarioActual;

    public FormulaServiceImpl(FormulaMedicaRepository formulaRepository,
                              FormulaMedicaDetalleRepository detalleRepository,
                              FormulaHistorialEstadoRepository historialRepository,
                              DespachoRepository despachoRepository, CitaRepository citaRepository,
                              AtencionMedicaRepository atencionRepository,
                              MedicamentoRepository medicamentoRepository, LoteMedicamentoRepository loteRepository,
                              UsuarioRepository usuarioRepository, ConfiguracionService configuracion,
                              UsuarioActual usuarioActual) {
        this.formulaRepository = formulaRepository;
        this.detalleRepository = detalleRepository;
        this.historialRepository = historialRepository;
        this.despachoRepository = despachoRepository;
        this.citaRepository = citaRepository;
        this.atencionRepository = atencionRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.configuracion = configuracion;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public List<FormulaRs> listar(String texto, LocalDate desde, LocalDate hasta, EstadoFormula estado) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (usuario.esMedico() && usuario.medicoId() == null) {
            return List.of();
        }
        RangoFechas rango = RangoFechas.de(desde, hasta);
        String filtro = texto == null ? "" : Texto.normalizar(texto);
        return convertir(formulaRepository.buscar(usuario.esMedico() ? usuario.medicoId() : null, estado,
                rango.inicio(), rango.finExclusivo()).stream()
                .filter(f -> Texto.normalizar(f.getNumero() + " " + f.getMascota().getNombreMascota() + " "
                        + f.getCliente().getNombres() + " " + f.getCliente().getApellidos()).contains(filtro))
                .toList());
    }

    @Override
    public List<FormulaRs> listarPorCitas(List<Long> citaIds) {
        return convertir(formulaRepository.findByCita_IdInOrderByFechaGeneracionDesc(citaIds));
    }

    @Override
    public List<FormulaRs> listarPorCita(Long citaId) {
        Cita cita = citaRepository.findById(citaId).orElseThrow(() -> new NotFoundException("La cita no existe."));
        usuarioActual.verificarPropiedad(cita.getMedico().getId());
        return listarPorCitas(List.of(citaId));
    }

    @Override
    public List<FormulaRs> antecedentes(Integer mascotaId, Long citaExcluida) {
        return convertir(formulaRepository.findByMascota_MascotaIdAndEstadoFormulaNotOrderByFechaGeneracionDesc(
                mascotaId, EstadoFormula.BORRADOR).stream()
                .filter(f -> !f.getCita().getId().equals(citaExcluida)).toList());
    }

    @Override
    public FormulaRs obtener(Long id) {
        return convertir(visible(id));
    }

    @Override
    @Transactional
    public FormulaRs crear(FormulaRq rq) {
        Cita cita = citaRepository.findById(rq.citaId())
                .orElseThrow(() -> new NotFoundException("La cita no existe."));
        usuarioActual.verificarPropiedad(cita.getMedico().getId());
        AtencionMedica atencion = atencionParaFormular(cita);
        FormulaMedica formula = new FormulaMedica();
        formula.setCita(cita);
        formula.setAtencion(atencion);
        formula.setMedico(cita.getMedico());
        formula.setMascota(cita.getMascota());
        formula.setCliente(cita.getCliente());
        formula.setFechaGeneracion(LocalDateTime.now());
        formula.setIndicacionesGenerales(rq.indicacionesGenerales());
        formula.setEstadoFormula(EstadoFormula.BORRADOR);
        formula = formulaRepository.saveAndFlush(formula);
        guardarDetalles(formula, rq.detalles());
        registrar(formula, null, null, null);
        return convertir(formula);
    }

    @Override
    @Transactional
    public FormulaRs actualizar(Long id, FormulaRq rq) {
        FormulaMedica formula = bloquear(id);
        exigirBorrador(formula);
        if (!formula.getCita().getId().equals(rq.citaId())) {
            throw new ConflictException("citaId", "La fórmula pertenece a otra cita.");
        }
        formula.setIndicacionesGenerales(rq.indicacionesGenerales());
        formulaRepository.saveAndFlush(formula);
        detalleRepository.eliminarPorFormula(id);
        guardarDetalles(formula, rq.detalles());
        return convertir(formula);
    }

    @Override
    @Transactional
    public FormulaRs confirmar(Long id) {
        FormulaMedica formula = bloquear(id);
        exigirBorrador(formula);
        atencionParaFormular(formula.getCita());
        detalleRepository.findByFormula_IdInOrderById(List.of(id))
                .forEach(d -> exigirActivo(d.getMedicamento()));
        formula.setNumero(configuracion.siguienteFormula());
        formula.setEstadoFormula(EstadoFormula.CONFIRMADA);
        formula.setEstadoDespacho(EstadoDespacho.PENDIENTE_DESPACHO);
        formula.setFechaConfirmacion(LocalDateTime.now());
        formulaRepository.saveAndFlush(formula);
        registrar(formula, EstadoFormula.BORRADOR, null, null);
        return convertir(formula);
    }

    /** No se puede si ya se entrego algo. Si hay una preparacion abierta, se cancela. */
    @Override
    @Transactional
    public FormulaRs anular(Long id, String motivo) {
        FormulaMedica formula = bloquear(id);
        if (formula.getEstadoFormula() == EstadoFormula.ANULADA) {
            throw new ConflictException("La fórmula ya está anulada.");
        }
        if (detalleRepository.findByFormula_IdInOrderById(List.of(id)).stream()
                .anyMatch(d -> d.getCantidadDespachada() > 0)) {
            throw new ConflictException("La fórmula tiene medicamentos entregados; no se puede anular.");
        }
        EstadoFormula anterior = formula.getEstadoFormula();
        EstadoDespacho despachoAnterior = formula.getEstadoDespacho();
        despachoRepository.findByFormula_IdAndEstado(id, EstadoPreparacion.EN_PREPARACION).forEach(d -> {
            d.setEstado(EstadoPreparacion.CANCELADO);
            d.setObservacion("Fórmula anulada");
            despachoRepository.save(d);
        });
        formula.setEstadoFormula(EstadoFormula.ANULADA);
        formula.setMotivoAnulacion(motivo.trim());
        if (despachoAnterior != null) {
            formula.setEstadoDespacho(EstadoDespacho.CANCELADO);
        }
        formulaRepository.saveAndFlush(formula);
        registrar(formula, anterior, despachoAnterior, motivo.trim());
        return convertir(formula);
    }

    @Override
    public Descarga pdf(Long id) {
        FormulaMedica formula = visible(id);
        return FormulaPdf.generar(configuracion.obtener(), formula,
                detalleRepository.findByFormula_IdInOrderById(List.of(id)));
    }

    private FormulaMedica visible(Long id) {
        FormulaMedica formula = formulaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La fórmula no existe."));
        UsuarioAutenticado usuario = usuarioActual.obtener();
        usuarioActual.verificarPropiedad(formula.getMedico().getId());
        if (Rol.DESPACHO_MEDICO.equals(usuario.rol()) && formula.getEstadoFormula() == EstadoFormula.BORRADOR) {
            throw new AccessDeniedException("Despacho no consulta borradores.");
        }
        return formula;
    }

    private FormulaMedica bloquear(Long id) {
        FormulaMedica formula = formulaRepository.bloquear(id)
                .orElseThrow(() -> new NotFoundException("La fórmula no existe."));
        usuarioActual.verificarPropiedad(formula.getMedico().getId());
        return formula;
    }

    /** Se formula durante la atencion o, para una formula olvidada, el mismo dia en que se finalizo. */
    private AtencionMedica atencionParaFormular(Cita cita) {
        AtencionMedica atencion = atencionRepository.findByCita_Id(cita.getId()).orElse(null);
        boolean enAtencion = cita.getEstado() == EstadoCita.EN_ATENCION;
        boolean atendidaHoy = cita.getEstado() == EstadoCita.ATENDIDA && atencion != null
                && atencion.getFechaFin() != null && atencion.getFechaFin().toLocalDate().equals(LocalDate.now());
        if (atencion == null || !enAtencion && !atendidaHoy) {
            throw new ConflictException("Solo se formula durante la atención o el mismo día en que se finalizó.");
        }
        return atencion;
    }

    private void guardarDetalles(FormulaMedica formula, List<FormulaRq.Detalle> detalles) {
        Set<Long> vistos = new HashSet<>();
        for (FormulaRq.Detalle d : detalles) {
            if (!vistos.add(d.medicamentoId())) {
                throw new ConflictException("medicamentoId", "Hay medicamentos repetidos en la fórmula.");
            }
            Medicamento medicamento = medicamentoRepository.findById(d.medicamentoId())
                    .orElseThrow(() -> new NotFoundException("El medicamento no existe."));
            exigirActivo(medicamento);
            FormulaMedicaDetalle detalle = new FormulaMedicaDetalle();
            detalle.setFormula(formula);
            detalle.setMedicamento(medicamento);
            detalle.setDosis(d.dosis());
            detalle.setUnidadDosis(d.unidadDosis().trim());
            detalle.setFrecuenciaHoras(d.frecuenciaHoras());
            detalle.setDuracionDias(d.duracionDias());
            detalle.setViaAdministracion(d.viaAdministracion());
            detalle.setCantidadPrescrita(d.cantidadPrescrita() == null ? d.cantidadSugerida() : d.cantidadPrescrita());
            detalle.setIndicaciones(d.indicaciones());
            detalleRepository.save(detalle);
        }
        detalleRepository.flush();
    }

    private static void exigirActivo(Medicamento m) {
        if (!Boolean.TRUE.equals(m.getActivo())) {
            throw new ConflictException("medicamentoId", "El medicamento " + m.getNombre() + " está inactivo.");
        }
    }

    private static void exigirBorrador(FormulaMedica formula) {
        if (formula.getEstadoFormula() != EstadoFormula.BORRADOR) {
            throw new ConflictException("La fórmula está " + formula.getEstadoFormula() + " y ya no se modifica.");
        }
    }

    private void registrar(FormulaMedica formula, EstadoFormula anterior, EstadoDespacho despachoAnterior,
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

    private FormulaRs convertir(FormulaMedica formula) {
        return convertir(List.of(formula)).getFirst();
    }

    private List<FormulaRs> convertir(List<FormulaMedica> formulas) {
        List<FormulaMedicaDetalle> todos = detalleRepository
                .findByFormula_IdInOrderById(formulas.stream().map(FormulaMedica::getId).toList());
        Map<Long, Integer> existencias = new HashMap<>();
        loteRepository.existenciasVigentes(todos.stream().map(d -> d.getMedicamento().getId()).distinct().toList(),
                LocalDate.now()).forEach(f -> existencias.put((Long) f[0], ((Number) f[1]).intValue()));
        Map<Long, List<FormulaRs.Detalle>> detalles = todos.stream().collect(Collectors.groupingBy(
                d -> d.getFormula().getId(), Collectors.mapping(d -> FormulaRs.Detalle.de(d,
                        existencias.getOrDefault(d.getMedicamento().getId(), 0)), Collectors.toList())));
        return formulas.stream()
                .map(formula -> FormulaRs.de(formula, detalles.getOrDefault(formula.getId(), List.of())))
                .toList();
    }
}
