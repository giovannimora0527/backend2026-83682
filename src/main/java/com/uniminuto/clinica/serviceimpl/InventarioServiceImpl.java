package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Impuesto;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.enums.EstadoStock;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import com.uniminuto.clinica.entity.enums.TipoProducto;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.AjusteRq;
import com.uniminuto.clinica.models.AlertasRs;
import com.uniminuto.clinica.models.DisponibilidadRs;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.models.LoteRs;
import com.uniminuto.clinica.models.MovimientoRs;
import com.uniminuto.clinica.models.ProductoRq;
import com.uniminuto.clinica.models.ProductoRs;
import com.uniminuto.clinica.repository.ImpuestoRepository;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.InventarioService;
import com.uniminuto.clinica.util.RangoFechas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ImpuestoRepository impuestoRepository;
    private final ExistenciasService existencias;
    private final UsuarioActual usuarioActual;
    private final int diasProximoVencer;

    public InventarioServiceImpl(MedicamentoRepository medicamentoRepository,
                                 LoteMedicamentoRepository loteRepository,
                                 MovimientoInventarioRepository movimientoRepository,
                                 ImpuestoRepository impuestoRepository, ExistenciasService existencias,
                                 UsuarioActual usuarioActual,
                                 @Value("${inventario.dias-proximo-vencer:30}") int diasProximoVencer) {
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.impuestoRepository = impuestoRepository;
        this.existencias = existencias;
        this.usuarioActual = usuarioActual;
        this.diasProximoVencer = diasProximoVencer;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRs> listar(String texto, TipoProducto tipo, EstadoStock estadoStock, boolean soloActivos) {
        String filtro = texto == null ? "" : normalizar(texto);
        return todos().stream()
                .filter(p -> !soloActivos || Boolean.TRUE.equals(p.activo()))
                .filter(p -> tipo == null || p.tipoProducto() == tipo)
                .filter(p -> estadoStock == null || p.estadosStock().contains(estadoStock))
                .filter(p -> normalizar(p.codigo() + " " + p.nombre()).contains(filtro))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoRs obtener(Long id) {
        return detalle(buscar(id));
    }

    @Override
    @Transactional
    public ProductoRs crear(ProductoRq rq) {
        Medicamento producto = new Medicamento();
        producto.setFechaCreacionRegistro(LocalDateTime.now());
        copiar(rq, producto);
        producto = medicamentoRepository.saveAndFlush(producto);
        if (rq.loteInicial() != null) {
            existencias.ingresar(producto, rq.loteInicial(), TipoMovimiento.ENTRADA_COMPRA, null, usuarioId());
        }
        return detalle(producto);
    }

    @Override
    @Transactional
    public ProductoRs actualizar(Long id, ProductoRq rq) {
        Medicamento producto = existencias.bloquearProducto(id);
        if (rq.loteInicial() != null) {
            throw new BadRequestException("Editar un producto no cambia sus existencias; registre una entrada.");
        }
        if (rq.tipoProducto() == TipoProducto.MEDICAMENTO && producto.getTipoProducto() != TipoProducto.MEDICAMENTO
                && loteRepository.findByMedicamento_IdOrderByFechaIngresoDescIdDesc(id).stream()
                .anyMatch(l -> l.getFechaVencimiento() == null)) {
            throw new ConflictException("tipoProducto",
                    "El producto tiene lotes sin vencimiento; no puede pasar a MEDICAMENTO.");
        }
        copiar(rq, producto);
        producto.setFechaModificacionRegistro(LocalDateTime.now());
        return detalle(medicamentoRepository.saveAndFlush(producto));
    }

    @Override
    @Transactional
    public ProductoRs cambiarEstado(Long id, boolean activo) {
        Medicamento producto = existencias.bloquearProducto(id);
        producto.setActivo(activo);
        producto.setFechaModificacionRegistro(LocalDateTime.now());
        return detalle(medicamentoRepository.saveAndFlush(producto));
    }

    @Override
    @Transactional
    public ProductoRs ingresarLote(Long id, LoteRq rq) {
        Medicamento producto = existencias.bloquearProducto(id);
        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new ConflictException("El producto está inactivo.");
        }
        existencias.ingresar(producto, rq, TipoMovimiento.ENTRADA_COMPRA, null, usuarioId());
        return detalle(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteRs> lotes(Long id, boolean soloVigentes) {
        buscar(id);
        LocalDate hoy = LocalDate.now();
        return loteRepository.findByMedicamento_IdOrderByFechaIngresoDescIdDesc(id).stream()
                .filter(l -> !soloVigentes || vigente(l, hoy))
                .map(l -> LoteRs.de(l, hoy)).toList();
    }

    @Override
    @Transactional
    public ProductoRs ajustar(Long id, AjusteRq rq) {
        Medicamento producto = existencias.bloquearProducto(id);
        LoteMedicamento lote = loteRepository.bloquear(rq.loteId())
                .filter(l -> l.getMedicamento().getId().equals(id))
                .orElseThrow(() -> new NotFoundException("El lote no existe en este producto."));
        existencias.ajustar(lote, rq.tipo(), rq.cantidad(), rq.motivo(), usuarioId());
        return detalle(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoRs> movimientos(Long medicamentoId, TipoMovimiento tipo, LocalDate desde,
                                          LocalDate hasta) {
        if (medicamentoId != null) {
            buscar(medicamentoId);
        }
        RangoFechas rango = RangoFechas.de(desde, hasta);
        return movimientoRepository.buscar(medicamentoId, tipo, rango.inicio(), rango.finExclusivo()).stream()
                .map(MovimientoRs::de).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertasRs alertas() {
        List<ProductoRs> activos = todos().stream().filter(p -> Boolean.TRUE.equals(p.activo())).toList();
        return new AlertasRs(contar(activos, EstadoStock.AGOTADO), contar(activos, EstadoStock.BAJO),
                contar(activos, EstadoStock.PROXIMO_VENCER), contar(activos, EstadoStock.VENCIDO));
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadRs disponibilidad(Long medicamentoId) {
        buscar(medicamentoId);
        LocalDate hoy = LocalDate.now();
        List<LoteMedicamento> conExistencia = loteRepository
                .findByMedicamento_IdOrderByFechaIngresoDescIdDesc(medicamentoId).stream()
                .filter(l -> Boolean.TRUE.equals(l.getActivo()) && l.getCantidadDisponible() > 0).toList();
        List<LoteMedicamento> vigentes = conExistencia.stream().filter(l -> vigente(l, hoy))
                .sorted(Comparator.comparing(LoteMedicamento::getFechaVencimiento,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(LoteMedicamento::getFechaIngreso).thenComparing(LoteMedicamento::getId))
                .toList();
        int total = sumar(conExistencia);
        int disponible = sumar(vigentes);
        return new DisponibilidadRs(medicamentoId, disponible, total - disponible,
                vigentes.stream().map(l -> LoteRs.de(l, hoy)).toList());
    }

    private List<ProductoRs> todos() {
        Map<Long, List<LoteMedicamento>> lotes = loteRepository.findAll().stream()
                .collect(Collectors.groupingBy(l -> l.getMedicamento().getId()));
        return medicamentoRepository.findAll().stream()
                .map(m -> convertir(m, lotes.getOrDefault(m.getId(), List.of()), false))
                .sorted(Comparator.comparing(ProductoRs::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private ProductoRs detalle(Medicamento producto) {
        return convertir(producto,
                loteRepository.findByMedicamento_IdOrderByFechaIngresoDescIdDesc(producto.getId()), true);
    }

    private ProductoRs convertir(Medicamento m, List<LoteMedicamento> lotes, boolean incluirLotes) {
        LocalDate hoy = LocalDate.now();
        List<LoteMedicamento> activos = lotes.stream().filter(l -> Boolean.TRUE.equals(l.getActivo())).toList();
        int cantidad = sumar(activos);
        BigDecimal ultimo = activos.stream()
                .max(Comparator.comparing(LoteMedicamento::getFechaIngreso).thenComparing(LoteMedicamento::getId))
                .map(LoteMedicamento::getPrecioCompra).orElse(null);
        BigDecimal promedio = cantidad == 0 ? null : activos.stream()
                .map(l -> l.getPrecioCompra().multiply(BigDecimal.valueOf(l.getCantidadDisponible())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
        LocalDate proximo = activos.stream().filter(l -> l.getCantidadDisponible() > 0 && vigente(l, hoy))
                .map(LoteMedicamento::getFechaVencimiento).filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);
        return new ProductoRs(m.getId(), m.getCodigo(), m.getNombre(), m.getTipoProducto(), m.getDescripcion(),
                m.getPresentacion(), m.getUnidadMedida(), m.getRequiereInvima(), m.getRegistroInvima(),
                m.getPrecioVenta(), m.getImpuesto() == null ? null : m.getImpuesto().getId(),
                m.getImpuesto() == null ? BigDecimal.ZERO : m.getImpuesto().getPorcentaje(), m.getStockMinimo(),
                m.getFechaProximoPedido(), m.getActivo(), cantidad, ultimo, promedio, proximo,
                estados(m, activos, cantidad, proximo, hoy),
                incluirLotes ? lotes.stream().map(l -> LoteRs.de(l, hoy)).toList() : null);
    }

    private List<EstadoStock> estados(Medicamento m, List<LoteMedicamento> activos, int cantidad, LocalDate proximo,
                                      LocalDate hoy) {
        List<EstadoStock> estados = new ArrayList<>();
        if (cantidad == 0) {
            estados.add(EstadoStock.AGOTADO);
        } else if (cantidad <= m.getStockMinimo()) {
            estados.add(EstadoStock.BAJO);
        }
        if (proximo != null && !proximo.isAfter(hoy.plusDays(diasProximoVencer))) {
            estados.add(EstadoStock.PROXIMO_VENCER);
        }
        if (activos.stream().anyMatch(l -> l.getCantidadDisponible() > 0 && !vigente(l, hoy))) {
            estados.add(EstadoStock.VENCIDO);
        }
        return estados.isEmpty() ? List.of(EstadoStock.DISPONIBLE) : estados;
    }

    private void copiar(ProductoRq rq, Medicamento producto) {
        String codigo = rq.codigo().toUpperCase(Locale.ROOT);
        String nombre = rq.nombre().trim();
        medicamentoRepository.findByCodigo(codigo).filter(m -> !m.getId().equals(producto.getId()))
                .ifPresent(m -> {
                    throw new ConflictException("codigo", "Ya existe un producto con ese código.");
                });
        medicamentoRepository.findByNombreIgnoreCase(nombre).filter(m -> !m.getId().equals(producto.getId()))
                .ifPresent(m -> {
                    throw new ConflictException("nombre", "Ya existe un producto con ese nombre.");
                });
        boolean medicamento = rq.tipoProducto() == TipoProducto.MEDICAMENTO;
        boolean requiere = medicamento || Boolean.TRUE.equals(rq.requiereInvima());
        String invima = vacioANulo(rq.registroInvima());
        if (requiere && invima == null) {
            throw new BadRequestException("El registro Invima es obligatorio para este producto.");
        }
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setTipoProducto(rq.tipoProducto());
        producto.setDescripcion(vacioANulo(rq.descripcion()));
        producto.setPresentacion(vacioANulo(rq.presentacion()));
        producto.setUnidadMedida(vacioANulo(rq.unidadMedida()) == null ? "unidad" : rq.unidadMedida().trim());
        producto.setRequiereInvima(requiere);
        producto.setRegistroInvima(invima);
        producto.setPrecioVenta(rq.precioVenta());
        producto.setImpuesto(buscarImpuesto(rq.impuestoId(), rq.porcentajeIva()));
        producto.setStockMinimo(rq.stockMinimo());
        producto.setFechaProximoPedido(rq.fechaProximoPedido());
    }

    private Impuesto buscarImpuesto(Long id, BigDecimal porcentaje) {
        if (id == null && porcentaje == null) {
            return null;
        }
        return impuestoRepository.findByActivoTrueOrderByPorcentaje().stream()
                .filter(i -> id != null ? i.getId().equals(id) : i.getPorcentaje().compareTo(porcentaje) == 0)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No hay un impuesto activo con ese valor."));
    }

    private Medicamento buscar(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El producto no existe."));
    }

    private Long usuarioId() {
        return usuarioActual.obtener().id();
    }

    private static boolean vigente(LoteMedicamento l, LocalDate hoy) {
        return l.getFechaVencimiento() == null || !l.getFechaVencimiento().isBefore(hoy);
    }

    private static int sumar(List<LoteMedicamento> lotes) {
        return lotes.stream().mapToInt(LoteMedicamento::getCantidadDisponible).sum();
    }

    private static long contar(List<ProductoRs> productos, EstadoStock estado) {
        return productos.stream().filter(p -> p.estadosStock().contains(estado)).count();
    }

    private static String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String normalizar(String valor) {
        return java.text.Normalizer.normalize(valor.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
