package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.entity.enums.EstadoPreparacion;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.exception.dto.ErrorResponse;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.DespachoConfirmarRq;
import com.uniminuto.clinica.models.DespachoFormulaRs;
import com.uniminuto.clinica.models.DespachoMedicamentoRs;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.FormulaHistorialRs;
import com.uniminuto.clinica.models.FormulaPendienteRs;
import com.uniminuto.clinica.pdf.ComprobanteDespachoPdf;
import com.uniminuto.clinica.repository.DespachoDetalleRepository;
import com.uniminuto.clinica.repository.DespachoRepository;
import com.uniminuto.clinica.repository.FormulaHistorialEstadoRepository;
import com.uniminuto.clinica.repository.FormulaMedicaDetalleRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.Rol;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.ConfiguracionService;
import com.uniminuto.clinica.service.DespachoService;
import com.uniminuto.clinica.util.RangoFechas;
import com.uniminuto.clinica.util.Texto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Despacho de formulas (R5, 5.7). Orden de bloqueo en todas las operaciones: formula, despacho, productos y lotes
 * (estos por id ascendente), para que no haya interbloqueos.
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class DespachoServiceImpl implements DespachoService {

    private static final Set<EstadoDespacho> PENDIENTES = Set.of(EstadoDespacho.PENDIENTE_DESPACHO,
            EstadoDespacho.EN_PREPARACION, EstadoDespacho.DESPACHO_PARCIAL, EstadoDespacho.PENDIENTE_DISPONIBILIDAD);

    private final DespachoRepository despachoRepository;
    private final DespachoDetalleRepository despachoDetalleRepository;
    private final FormulaMedicaRepository formulaRepository;
    private final FormulaMedicaDetalleRepository formulaDetalleRepository;
    private final FormulaHistorialEstadoRepository historialRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExistenciasService existencias;
    private final HistorialFormulaService historialFormula;
    private final ConfiguracionService configuracion;
    private final UsuarioActual usuarioActual;
    private final int diasProximoVencer;

    public DespachoServiceImpl(DespachoRepository despachoRepository,
                               DespachoDetalleRepository despachoDetalleRepository,
                               FormulaMedicaRepository formulaRepository,
                               FormulaMedicaDetalleRepository formulaDetalleRepository,
                               FormulaHistorialEstadoRepository historialRepository,
                               MedicamentoRepository medicamentoRepository, LoteMedicamentoRepository loteRepository,
                               MovimientoInventarioRepository movimientoRepository,
                               UsuarioRepository usuarioRepository, ExistenciasService existencias,
                               HistorialFormulaService historialFormula, ConfiguracionService configuracion,
                               UsuarioActual usuarioActual,
                               @Value("${inventario.dias-proximo-vencer:30}") int diasProximoVencer) {
        this.despachoRepository = despachoRepository;
        this.despachoDetalleRepository = despachoDetalleRepository;
        this.formulaRepository = formulaRepository;
        this.formulaDetalleRepository = formulaDetalleRepository;
        this.historialRepository = historialRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.existencias = existencias;
        this.historialFormula = historialFormula;
        this.configuracion = configuracion;
        this.usuarioActual = usuarioActual;
        this.diasProximoVencer = diasProximoVencer;
    }

    // ------------------------------------------------------------ consultas

    @Override
    @Transactional(readOnly = true)
    public List<FormulaPendienteRs> formulasPendientes(String texto, String numero, LocalDate desde, LocalDate hasta,
                                                       EstadoDespacho estado) {
        RangoFechas rango = RangoFechas.de(desde, hasta);
        String filtro = texto == null ? "" : Texto.normalizar(texto);
        String filtroNumero = numero == null ? "" : Texto.normalizar(numero);
        List<FormulaMedica> formulas = formulaRepository.bandeja(estado == null ? PENDIENTES : Set.of(estado),
                rango.inicio(), rango.finExclusivo()).stream()
                .filter(f -> Texto.normalizar(f.getNumero()).contains(filtroNumero))
                .filter(f -> Texto.normalizar(f.getNumero() + " " + propietario(f) + " "
                        + f.getMascota().getNombreMascota()).contains(filtro))
                .toList();
        List<Long> ids = formulas.stream().map(FormulaMedica::getId).toList();
        Map<Long, List<FormulaMedicaDetalle>> detalles = formulaDetalleRepository.findByFormula_IdInOrderById(ids)
                .stream().collect(Collectors.groupingBy(d -> d.getFormula().getId()));
        Map<Long, String> preparando = despachoRepository
                .findByFormula_IdInAndEstado(ids, EstadoPreparacion.EN_PREPARACION).stream()
                .collect(Collectors.toMap(d -> d.getFormula().getId(), d -> d.getUsuario().getUsername(), (a, b) -> a));
        return formulas.stream().map(f -> {
            List<FormulaMedicaDetalle> propios = detalles.getOrDefault(f.getId(), List.of());
            return new FormulaPendienteRs(f.getId(), f.getNumero(), f.getFechaGeneracion(), medico(f), propietario(f),
                    f.getMascota().getNombreMascota(), propios.size(), f.getIndicacionesGenerales(),
                    f.getEstadoDespacho(), preparando.get(f.getId()), propios.stream()
                    .map(d -> new FormulaPendienteRs.Formulado(d.getMedicamento().getNombre(), d.getDosis(),
                            d.getUnidadDosis(), d.getFrecuenciaHoras(), d.getDuracionDias(), d.getIndicaciones(),
                            d.getCantidadPrescrita(), d.getCantidadDespachada())).toList());
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DespachoFormulaRs consultar(Long formulaId) {
        FormulaMedica formula = formulaRepository.findById(formulaId)
                .orElseThrow(() -> new NotFoundException("La fórmula no existe."));
        if (formula.getEstadoFormula() == EstadoFormula.BORRADOR) {
            throw new NotFoundException("La fórmula no existe.");
        }
        return preparacion(formula);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespachoRs> historial(LocalDate desde, LocalDate hasta, String texto, String estado) {
        RangoFechas rango = RangoFechas.de(desde, hasta);
        String filtro = texto == null ? "" : Texto.normalizar(texto);
        validarFiltroEstado(estado);
        List<Despacho> despachos = despachoRepository.historial(rango.inicio(), rango.finExclusivo()).stream()
                .filter(d -> estado == null || estado.isBlank() || d.getEstado().name().equals(estado)
                        || d.getFormula().getEstadoDespacho().name().equals(estado))
                .filter(d -> Texto.normalizar(d.getNumero() + " " + d.getFormula().getNumero() + " "
                        + propietario(d.getFormula()) + " " + d.getFormula().getMascota().getNombreMascota())
                        .contains(filtro))
                .toList();
        return convertir(despachos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DespachoMedicamentoRs> historialMedicamento(Long medicamentoId) {
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new NotFoundException("El producto no existe.");
        }
        List<DespachoDetalle> detalles = despachoDetalleRepository.historial(medicamentoId);
        List<MovimientoInventario> movimientos = movimientoRepository.findByDespachoDetalle_IdIn(
                detalles.stream().map(DespachoDetalle::getId).toList());
        Map<Long, MovimientoInventario> salidas = porDetalle(movimientos, TipoMovimiento.SALIDA_DESPACHO);
        Map<Long, MovimientoInventario> reversiones = porDetalle(movimientos, TipoMovimiento.ENTRADA_REVERSION);
        return detalles.stream().map(x -> {
            Despacho d = x.getDespacho();
            FormulaMedica f = d.getFormula();
            MovimientoInventario salida = salidas.get(x.getId());
            MovimientoInventario reversion = reversiones.get(x.getId());
            return new DespachoMedicamentoRs(d.getId(), d.getNumero(), f.getId(), f.getNumero(),
                    d.getFechaConfirmacion(), d.getUsuario().getUsername(), medico(f), propietario(f),
                    f.getMascota().getNombreMascota(), x.getLote().getId(), x.getLote().getNumeroLote(),
                    x.getCantidadEntregada(), d.getEstado(), salida == null ? null : salida.getId(),
                    salida == null ? null : salida.getSaldoLoteResultante(),
                    salida == null ? null : salida.getSaldoProductoResultante(),
                    reversion == null ? null : reversion.getId());
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormulaHistorialRs> historialEstados(Long formulaId) {
        if (!formulaRepository.existsById(formulaId)) {
            throw new NotFoundException("La fórmula no existe.");
        }
        return historialRepository.findByFormula_IdOrderByFechaAscIdAsc(formulaId).stream()
                .map(FormulaHistorialRs::de).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Descarga comprobante(Long despachoId) {
        Despacho despacho = despachoRepository.findById(despachoId)
                .orElseThrow(() -> new NotFoundException("El despacho no existe."));
        if (despacho.getEstado() != EstadoPreparacion.CONFIRMADO && despacho.getEstado() != EstadoPreparacion.REVERTIDO) {
            throw new ConflictException("Solo hay comprobante de despachos confirmados.");
        }
        List<DespachoDetalle> entregas = despachoDetalleRepository.findByDespacho_IdInOrderById(List.of(despachoId));
        Map<Long, Long> pendientes = new HashMap<>();
        List<FormulaMedicaDetalle> formulados = detalles(despacho.getFormula());
        formulados.forEach(d -> pendientes.put(d.getId(),
                d.getCantidadPrescrita() - despachoDetalleRepository.entregadoHasta(d.getId(), despachoId)));
        return ComprobanteDespachoPdf.generar(configuracion.obtener(), despacho, formulados, entregas, pendientes);
    }

    // ------------------------------------------------------------ operaciones

    @Override
    public DespachoFormulaRs preparar(Long formulaId) {
        FormulaMedica formula = bloquearFormula(formulaId);
        Long usuarioId = usuarioActual.obtener().id();
        Optional<Despacho> abierto = preparacionAbierta(formulaId);
        if (abierto.isPresent()) {
            if (!abierto.get().getUsuario().getId().equals(usuarioId)) {
                throw new ConflictException("La fórmula la está preparando "
                        + abierto.get().getUsuario().getUsername() + ".");
            }
            return preparacion(formula);
        }
        if (formula.getEstadoFormula() != EstadoFormula.CONFIRMADA || !PENDIENTES.contains(formula.getEstadoDespacho())) {
            throw new ConflictException("La fórmula no está pendiente de despacho.");
        }
        Despacho despacho = new Despacho();
        despacho.setFormula(formula);
        despacho.setNumero(configuracion.siguienteDespacho());
        despacho.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        despacho.setEstado(EstadoPreparacion.EN_PREPARACION);
        despacho.setFechaInicio(LocalDateTime.now());
        despachoRepository.saveAndFlush(despacho);
        cambiarEstado(formula, EstadoDespacho.EN_PREPARACION, null);
        return preparacion(formula);
    }

    @Override
    public DespachoRs confirmar(Long despachoId, DespachoConfirmarRq rq, String idempotencyKey) {
        String clave = clave(idempotencyKey);
        FormulaMedica formula = bloquearFormula(formulaDe(despachoId));
        Despacho despacho = despachoRepository.bloquear(despachoId).orElseThrow();
        if (clave != null) {
            Optional<Despacho> previo = despachoRepository.findByIdempotencyKey(clave);
            if (previo.isPresent()) {
                if (!previo.get().getId().equals(despachoId)) {
                    throw new ConflictException("La clave de idempotencia se usó en otro despacho.");
                }
                return convertir(previo.get());
            }
        }
        exigirEnPreparacion(despacho);
        exigirResponsable(despacho);
        List<FormulaMedicaDetalle> formulados = detalles(formula);
        List<DespachoConfirmarRq.Item> items = rq.items() == null ? List.of() : rq.items();
        if (items.isEmpty() && hayExistencias(formulados)) {
            throw new ConflictException("items", "Hay existencias por entregar: seleccione lotes o libere la preparación.");
        }
        Map<Long, LoteMedicamento> lotes = bloquearLotes(items, formulados);
        validar(items, formulados, lotes);
        Long usuarioId = usuarioActual.obtener().id();
        Map<Long, FormulaMedicaDetalle> porId = porId(formulados);
        for (DespachoConfirmarRq.Item item : items) {
            entregar(despacho, porId.get(item.formulaDetalleId()), lotes.get(item.loteId()), item.cantidad(), usuarioId);
        }
        formulaDetalleRepository.flush();
        despacho.setEstado(EstadoPreparacion.CONFIRMADO);
        despacho.setFechaConfirmacion(LocalDateTime.now());
        despacho.setObservacion(rq.observacion());
        despacho.setIdempotencyKey(clave);
        despachoRepository.saveAndFlush(despacho);
        cambiarEstado(formula, calcularEstado(formulados), rq.observacion());
        return convertir(despacho);
    }

    @Override
    public DespachoFormulaRs liberar(Long despachoId) {
        FormulaMedica formula = bloquearFormula(formulaDe(despachoId));
        Despacho despacho = despachoRepository.bloquear(despachoId).orElseThrow();
        exigirEnPreparacion(despacho);
        if (!esAdministrador()) {
            exigirResponsable(despacho);
        }
        cerrarPreparacion(despacho, "Preparación liberada");
        cambiarEstado(formula, calcularEstado(detalles(formula)), "Preparación liberada");
        return preparacion(formula);
    }

    @Override
    public DespachoFormulaRs cancelar(Long formulaId, String motivo) {
        FormulaMedica formula = bloquearFormula(formulaId);
        if (formula.getEstadoDespacho() == null || !PENDIENTES.contains(formula.getEstadoDespacho())) {
            throw new ConflictException("El despacho de la fórmula no se puede cancelar en su estado actual.");
        }
        if (!esAdministrador()
                && detalles(formula).stream().anyMatch(d -> d.getCantidadDespachada() > 0)) {
            throw new AccessDeniedException("Con entregas confirmadas solo cancela un administrador.");
        }
        preparacionAbierta(formulaId).ifPresent(d -> {
            if (!esAdministrador()) {
                exigirResponsable(d);
            }
            cerrarPreparacion(d, motivo.trim());
        });
        cambiarEstado(formula, EstadoDespacho.CANCELADO, motivo.trim());
        return preparacion(formula);
    }

    /** Nada se borra: cada salida se compensa con una entrada al mismo lote. */
    @Override
    public DespachoRs revertir(Long despachoId, String motivo) {
        FormulaMedica formula = bloquearFormula(formulaDe(despachoId));
        Despacho despacho = despachoRepository.bloquear(despachoId).orElseThrow();
        if (despacho.getEstado() != EstadoPreparacion.CONFIRMADO) {
            throw new ConflictException("Solo se revierten despachos confirmados; este se encuentra "
                    + despacho.getEstado() + ".");
        }
        List<DespachoDetalle> entregas = despachoDetalleRepository.findByDespacho_IdInOrderById(List.of(despachoId));
        if (entregas.isEmpty()) {
            throw new ConflictException("El despacho no entregó medicamentos; no hay nada que revertir.");
        }
        if (entregas.stream().anyMatch(d -> Boolean.TRUE.equals(d.getFacturado()))) {
            throw new ConflictException("El despacho tiene medicamentos facturados; anule primero la factura.");
        }
        new TreeSet<>(entregas.stream().map(d -> d.getMedicamento().getId()).toList())
                .forEach(existencias::bloquearProducto);
        Map<Long, LoteMedicamento> lotes = new HashMap<>();
        new TreeSet<>(entregas.stream().map(d -> d.getLote().getId()).toList())
                .forEach(id -> lotes.put(id, loteRepository.bloquear(id).orElseThrow()));
        Long usuarioId = usuarioActual.obtener().id();
        Map<Long, FormulaMedicaDetalle> porId = porId(detalles(formula));
        for (MovimientoInventario salida : movimientoRepository
                .findByDespacho_IdAndTipo(despachoId, TipoMovimiento.SALIDA_DESPACHO)) {
            existencias.revertir(salida, lotes.get(salida.getLote().getId()), motivo.trim(), usuarioId);
            FormulaMedicaDetalle detalle = porId.get(salida.getDespachoDetalle().getFormulaDetalle().getId());
            detalle.setCantidadDespachada(detalle.getCantidadDespachada() - salida.getCantidad());
            formulaDetalleRepository.save(detalle);
        }
        formulaDetalleRepository.flush();
        despacho.setEstado(EstadoPreparacion.REVERTIDO);
        despacho.setFechaReversion(LocalDateTime.now());
        despacho.setUsuarioReversion(usuarioRepository.getReferenceById(usuarioId));
        despacho.setMotivoReversion(motivo.trim());
        despachoRepository.saveAndFlush(despacho);
        boolean conservaEstado = formula.getEstadoDespacho() == EstadoDespacho.CANCELADO
                || formula.getEstadoDespacho() == EstadoDespacho.EN_PREPARACION;
        cambiarEstado(formula, conservaEstado ? formula.getEstadoDespacho()
                : calcularEstado(new ArrayList<>(porId.values())), "Reversión de " + despacho.getNumero() + ": "
                + motivo.trim());
        return convertir(despacho);
    }

    // ------------------------------------------------------------ reglas

    /** Reune todos los errores para indicar que item fallo; cualquiera impide toda la operacion. */
    private void validar(List<DespachoConfirmarRq.Item> items, List<FormulaMedicaDetalle> formulados,
                         Map<Long, LoteMedicamento> lotes) {
        LocalDate hoy = LocalDate.now();
        Map<Long, FormulaMedicaDetalle> porId = porId(formulados);
        Map<Long, Long> porDetalle = new HashMap<>();
        Map<Long, Long> porLote = new HashMap<>();
        Set<String> vistos = new HashSet<>();
        List<ErrorResponse.Detalle> errores = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            DespachoConfirmarRq.Item item = items.get(i);
            String campo = "items[" + i + "]";
            FormulaMedicaDetalle detalle = porId.get(item.formulaDetalleId());
            if (detalle == null) {
                errores.add(new ErrorResponse.Detalle(campo, "El detalle no pertenece a la fórmula."));
                continue;
            }
            String nombre = detalle.getMedicamento().getNombre();
            LoteMedicamento lote = lotes.get(item.loteId());
            if (lote == null || !lote.getMedicamento().getId().equals(detalle.getMedicamento().getId())
                    || !Boolean.TRUE.equals(lote.getActivo())) {
                errores.add(new ErrorResponse.Detalle(campo, "El lote no es de " + nombre + " o está inactivo."));
                continue;
            }
            if (!vistos.add(item.formulaDetalleId() + "-" + item.loteId())) {
                errores.add(new ErrorResponse.Detalle(campo, nombre + ": el lote está repetido."));
                continue;
            }
            if (vencido(lote, hoy)) {
                errores.add(new ErrorResponse.Detalle(campo, nombre + ": el lote " + lote.getNumeroLote()
                        + " está vencido."));
            }
            if (porLote.merge(lote.getId(), (long) item.cantidad(), Long::sum) > lote.getCantidadDisponible()) {
                errores.add(new ErrorResponse.Detalle(campo, nombre + ": el lote " + lote.getNumeroLote()
                        + " solo tiene " + lote.getCantidadDisponible() + "."));
            }
            if (porDetalle.merge(detalle.getId(), (long) item.cantidad(), Long::sum) > pendiente(detalle)) {
                errores.add(new ErrorResponse.Detalle(campo, nombre + ": solo hay " + pendiente(detalle)
                        + " pendientes por entregar."));
            }
        }
        if (!errores.isEmpty()) {
            throw new ConflictException("No se despachó nada: " + errores.stream()
                    .map(ErrorResponse.Detalle::mensaje).collect(Collectors.joining(" ")), errores);
        }
    }

    private Map<Long, LoteMedicamento> bloquearLotes(List<DespachoConfirmarRq.Item> items,
                                                     List<FormulaMedicaDetalle> formulados) {
        Map<Long, FormulaMedicaDetalle> porId = porId(formulados);
        new TreeSet<>(items.stream().map(i -> porId.get(i.formulaDetalleId())).filter(Objects::nonNull)
                .map(d -> d.getMedicamento().getId()).toList()).forEach(existencias::bloquearProducto);
        Map<Long, LoteMedicamento> lotes = new HashMap<>();
        new TreeSet<>(items.stream().map(DespachoConfirmarRq.Item::loteId).toList())
                .forEach(id -> loteRepository.bloquear(id).ifPresent(l -> lotes.put(id, l)));
        return lotes;
    }

    private void entregar(Despacho despacho, FormulaMedicaDetalle formulado, LoteMedicamento lote, int cantidad,
                          Long usuarioId) {
        Medicamento medicamento = formulado.getMedicamento();
        DespachoDetalle detalle = new DespachoDetalle();
        detalle.setDespacho(despacho);
        detalle.setFormulaDetalle(formulado);
        detalle.setMedicamento(medicamento);
        detalle.setLote(lote);
        detalle.setCantidadEntregada(cantidad);
        detalle.setPrecioVentaUnitario(medicamento.getPrecioVenta());
        detalle.setPorcentajeIva(medicamento.getImpuesto() == null ? BigDecimal.ZERO
                : medicamento.getImpuesto().getPorcentaje());
        detalle = despachoDetalleRepository.saveAndFlush(detalle);
        existencias.despachar(detalle, usuarioId);
        formulado.setCantidadDespachada(formulado.getCantidadDespachada() + cantidad);
        formulaDetalleRepository.save(formulado);
    }

    /** Tabla de decision en docs/reglas-negocio.md. */
    private EstadoDespacho calcularEstado(List<FormulaMedicaDetalle> formulados) {
        if (formulados.stream().allMatch(d -> pendiente(d) == 0)) {
            return EstadoDespacho.DESPACHADO;
        }
        if (formulados.stream().anyMatch(d -> d.getCantidadDespachada() > 0)) {
            return EstadoDespacho.DESPACHO_PARCIAL;
        }
        return hayExistencias(formulados) ? EstadoDespacho.PENDIENTE_DESPACHO
                : EstadoDespacho.PENDIENTE_DISPONIBILIDAD;
    }

    private boolean hayExistencias(List<FormulaMedicaDetalle> formulados) {
        Map<Long, Integer> vigentes = vigentes(formulados);
        return formulados.stream().anyMatch(d -> pendiente(d) > 0
                && vigentes.getOrDefault(d.getMedicamento().getId(), 0) > 0);
    }

    private Map<Long, Integer> vigentes(List<FormulaMedicaDetalle> formulados) {
        Map<Long, Integer> vigentes = new HashMap<>();
        loteRepository.existenciasVigentes(formulados.stream().map(d -> d.getMedicamento().getId()).toList(),
                LocalDate.now()).forEach(f -> vigentes.put((Long) f[0], ((Number) f[1]).intValue()));
        return vigentes;
    }

    private void cambiarEstado(FormulaMedica formula, EstadoDespacho nuevo, String motivo) {
        EstadoDespacho anterior = formula.getEstadoDespacho();
        formula.setEstadoDespacho(nuevo);
        formulaRepository.saveAndFlush(formula);
        historialFormula.registrar(formula, formula.getEstadoFormula(), anterior, motivo);
    }

    private void cerrarPreparacion(Despacho despacho, String motivo) {
        despacho.setEstado(EstadoPreparacion.CANCELADO);
        despacho.setObservacion(motivo);
        despachoRepository.saveAndFlush(despacho);
    }

    private void exigirEnPreparacion(Despacho despacho) {
        if (despacho.getEstado() != EstadoPreparacion.EN_PREPARACION) {
            throw new ConflictException("El despacho " + despacho.getNumero() + " ya está " + despacho.getEstado() + ".");
        }
    }

    private void exigirResponsable(Despacho despacho) {
        if (!despacho.getUsuario().getId().equals(usuarioActual.obtener().id())) {
            throw new AccessDeniedException("La preparación es de otro usuario.");
        }
    }

    private boolean esAdministrador() {
        return Rol.ADMINISTRADOR.equals(usuarioActual.obtener().rol());
    }

    private FormulaMedica bloquearFormula(Long id) {
        return formulaRepository.bloquear(id).orElseThrow(() -> new NotFoundException("La fórmula no existe."));
    }

    private Long formulaDe(Long despachoId) {
        return despachoRepository.formulaDe(despachoId)
                .orElseThrow(() -> new NotFoundException("El despacho no existe."));
    }

    private Optional<Despacho> preparacionAbierta(Long formulaId) {
        return despachoRepository.findByFormula_IdAndEstado(formulaId, EstadoPreparacion.EN_PREPARACION).stream()
                .findFirst();
    }

    private List<FormulaMedicaDetalle> detalles(FormulaMedica formula) {
        return formulaDetalleRepository.findByFormula_IdInOrderById(List.of(formula.getId()));
    }

    // ------------------------------------------------------------ conversion

    private DespachoFormulaRs preparacion(FormulaMedica f) {
        List<FormulaMedicaDetalle> formulados = detalles(f);
        Map<Long, List<LoteMedicamento>> lotes = loteRepository.conExistencias(formulados.stream()
                        .map(d -> d.getMedicamento().getId()).toList()).stream()
                .collect(Collectors.groupingBy(l -> l.getMedicamento().getId()));
        Despacho abierto = preparacionAbierta(f.getId()).orElse(null);
        return new DespachoFormulaRs(f.getId(), abierto == null ? null : abierto.getId(),
                abierto == null ? null : abierto.getNumero(),
                abierto == null ? null : usuarioRepository.findById(abierto.getUsuario().getId())
                        .map(Usuario::getUsername).orElse(null),
                f.getNumero(), f.getFechaGeneracion(), medico(f), propietario(f), f.getCliente().getNumeroDocumento(),
                f.getMascota().getNombreMascota(), f.getIndicacionesGenerales(), f.getEstadoDespacho(),
                formulados.stream().map(d -> detalle(d, lotes.getOrDefault(d.getMedicamento().getId(), List.of())))
                        .toList());
    }

    private DespachoFormulaRs.Detalle detalle(FormulaMedicaDetalle d, List<LoteMedicamento> lotes) {
        LocalDate hoy = LocalDate.now();
        int pendiente = pendiente(d);
        int porSugerir = pendiente;
        int vigente = 0;
        List<DespachoFormulaRs.Lote> sugeridos = new ArrayList<>();
        for (LoteMedicamento l : lotes.stream().sorted(Comparator
                .comparing((LoteMedicamento l) -> vencido(l, hoy))
                .thenComparing(LoteMedicamento::getFechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(LoteMedicamento::getFechaIngreso).thenComparing(LoteMedicamento::getId)).toList()) {
            boolean vencido = vencido(l, hoy);
            int sugerida = vencido ? 0 : Math.min(porSugerir, l.getCantidadDisponible());
            porSugerir -= sugerida;
            vigente += vencido ? 0 : l.getCantidadDisponible();
            sugeridos.add(new DespachoFormulaRs.Lote(l.getId(), l.getNumeroLote(), l.getFechaVencimiento(),
                    l.getCantidadDisponible(), vencido, !vencido && l.getFechaVencimiento() != null
                    && !l.getFechaVencimiento().isAfter(hoy.plusDays(diasProximoVencer)), sugerida));
        }
        Medicamento m = d.getMedicamento();
        return new DespachoFormulaRs.Detalle(d.getId(), m.getId(), m.getCodigo(), m.getNombre(), d.getDosis(),
                d.getUnidadDosis(), d.getFrecuenciaHoras(), d.getDuracionDias(), d.getViaAdministracion(),
                d.getIndicaciones(), d.getCantidadPrescrita(), d.getCantidadDespachada(), pendiente, vigente,
                pendiente > 0 && vigente > 0, sugeridos);
    }

    private DespachoRs convertir(Despacho despacho) {
        return convertir(List.of(despacho)).getFirst();
    }

    private List<DespachoRs> convertir(List<Despacho> despachos) {
        Map<Long, List<DespachoDetalle>> entregas = despachoDetalleRepository.findByDespacho_IdInOrderById(
                        despachos.stream().map(Despacho::getId).toList()).stream()
                .collect(Collectors.groupingBy(d -> d.getDespacho().getId()));
        return despachos.stream().map(d -> {
            FormulaMedica f = d.getFormula();
            return new DespachoRs(d.getId(), d.getNumero(), f.getId(), f.getNumero(),
                    d.getFechaConfirmacion() == null ? d.getFechaInicio() : d.getFechaConfirmacion(),
                    d.getFechaInicio(), f.getMascota().getNombreMascota(), propietario(f), medico(f),
                    d.getUsuario().getUsername(), d.getEstado(), f.getEstadoDespacho(), d.getObservacion(),
                    d.getEstado() == EstadoPreparacion.REVERTIDO, d.getFechaReversion(),
                    d.getUsuarioReversion() == null ? null : d.getUsuarioReversion().getUsername(),
                    d.getMotivoReversion(), entregas.getOrDefault(d.getId(), List.of()).stream()
                    .map(x -> new DespachoRs.Item(x.getFormulaDetalle().getId(), x.getMedicamento().getId(),
                            x.getMedicamento().getNombre(), x.getLote().getId(), x.getLote().getNumeroLote(),
                            x.getLote().getFechaVencimiento(), x.getCantidadEntregada(), x.getFacturado())).toList(),
                    sinExistencias(f));
        }).toList();
    }

    /** Medicamentos con cantidades pendientes y sin existencias vigentes en este momento. */
    private List<String> sinExistencias(FormulaMedica formula) {
        if (!PENDIENTES.contains(formula.getEstadoDespacho())) {
            return List.of();
        }
        List<FormulaMedicaDetalle> formulados = detalles(formula);
        Map<Long, Integer> vigentes = vigentes(formulados);
        return formulados.stream().filter(d -> pendiente(d) > 0
                        && vigentes.getOrDefault(d.getMedicamento().getId(), 0) == 0)
                .map(d -> d.getMedicamento().getNombre()).toList();
    }

    private static Map<Long, MovimientoInventario> porDetalle(List<MovimientoInventario> movimientos,
                                                              TipoMovimiento tipo) {
        return movimientos.stream().filter(m -> m.getTipo() == tipo)
                .collect(Collectors.toMap(m -> m.getDespachoDetalle().getId(), Function.identity(), (a, b) -> a));
    }

    private static Map<Long, FormulaMedicaDetalle> porId(List<FormulaMedicaDetalle> detalles) {
        return detalles.stream().collect(Collectors.toMap(FormulaMedicaDetalle::getId, Function.identity(),
                (a, b) -> a, LinkedHashMap::new));
    }

    private static int pendiente(FormulaMedicaDetalle d) {
        return d.getCantidadPrescrita() - d.getCantidadDespachada();
    }

    private static boolean vencido(LoteMedicamento lote, LocalDate hoy) {
        return lote.getFechaVencimiento() != null && lote.getFechaVencimiento().isBefore(hoy);
    }

    private static String propietario(FormulaMedica f) {
        return f.getCliente().getNombres() + " " + f.getCliente().getApellidos();
    }

    private static String medico(FormulaMedica f) {
        return f.getMedico().getNombres() + " " + f.getMedico().getApellidos();
    }

    private static String clave(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        if (valor.trim().length() > 64) {
            throw new BadRequestException("La clave de idempotencia admite hasta 64 caracteres.");
        }
        return valor.trim();
    }

    private static void validarFiltroEstado(String estado) {
        if (estado != null && !estado.isBlank()
                && Arrays.stream(EstadoPreparacion.values()).noneMatch(e -> e.name().equals(estado))
                && Arrays.stream(EstadoDespacho.values()).noneMatch(e -> e.name().equals(estado))) {
            throw new BadRequestException("El parametro estado no es valido.");
        }
    }
}
