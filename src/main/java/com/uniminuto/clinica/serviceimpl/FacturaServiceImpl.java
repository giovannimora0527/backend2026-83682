package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.entity.AtencionServicio;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.FacturaDetalle;
import com.uniminuto.clinica.entity.FacturaHistorialEstado;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Pago;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.entity.enums.EstadoDespacho;
import com.uniminuto.clinica.entity.enums.EstadoFactura;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.entity.enums.TipoItem;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.exception.dto.ErrorResponse;
import com.uniminuto.clinica.models.CitaPendienteFacturaRs;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.FacturaDetalleRs;
import com.uniminuto.clinica.models.FacturaGenerarRq;
import com.uniminuto.clinica.models.FacturaHistorialRs;
import com.uniminuto.clinica.models.FacturaPreviaRs;
import com.uniminuto.clinica.models.FacturaRs;
import com.uniminuto.clinica.models.PagoRq;
import com.uniminuto.clinica.models.PagoRs;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.pdf.DocumentoPdf;
import com.uniminuto.clinica.pdf.FacturaPdf;
import com.uniminuto.clinica.repository.AtencionMedicaRepository;
import com.uniminuto.clinica.repository.AtencionServicioRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.DespachoDetalleRepository;
import com.uniminuto.clinica.repository.FacturaDetalleRepository;
import com.uniminuto.clinica.repository.FacturaHistorialEstadoRepository;
import com.uniminuto.clinica.repository.FacturaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.PagoRepository;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.Rol;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.ConfiguracionService;
import com.uniminuto.clinica.service.FacturaService;
import com.uniminuto.clinica.util.RangoFechas;
import com.uniminuto.clinica.util.RenglonCobro;
import com.uniminuto.clinica.util.Texto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Facturacion y pagos (5.8). Orden de bloqueo: cita, formulas y configuracion; pagos y anulacion, la factura. */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class FacturaServiceImpl implements FacturaService {

    private static final Set<EstadoDespacho> DESPACHO_PENDIENTE = Set.of(EstadoDespacho.PENDIENTE_DESPACHO,
            EstadoDespacho.EN_PREPARACION, EstadoDespacho.DESPACHO_PARCIAL, EstadoDespacho.PENDIENTE_DISPONIBILIDAD);

    private final FacturaRepository facturaRepository;
    private final FacturaDetalleRepository detalleRepository;
    private final PagoRepository pagoRepository;
    private final FacturaHistorialEstadoRepository historialRepository;
    private final CitaRepository citaRepository;
    private final AtencionMedicaRepository atencionRepository;
    private final AtencionServicioRepository atencionServicioRepository;
    private final TarifaRepository tarifaRepository;
    private final FormulaMedicaRepository formulaRepository;
    private final DespachoDetalleRepository despachoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionService configuracion;
    private final UsuarioActual usuarioActual;

    public FacturaServiceImpl(FacturaRepository facturaRepository, FacturaDetalleRepository detalleRepository,
                              PagoRepository pagoRepository, FacturaHistorialEstadoRepository historialRepository,
                              CitaRepository citaRepository, AtencionMedicaRepository atencionRepository,
                              AtencionServicioRepository atencionServicioRepository,
                              TarifaRepository tarifaRepository, FormulaMedicaRepository formulaRepository,
                              DespachoDetalleRepository despachoDetalleRepository,
                              ClienteRepository clienteRepository, MascotaRepository mascotaRepository,
                              UsuarioRepository usuarioRepository, ConfiguracionService configuracion,
                              UsuarioActual usuarioActual) {
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.pagoRepository = pagoRepository;
        this.historialRepository = historialRepository;
        this.citaRepository = citaRepository;
        this.atencionRepository = atencionRepository;
        this.atencionServicioRepository = atencionServicioRepository;
        this.tarifaRepository = tarifaRepository;
        this.formulaRepository = formulaRepository;
        this.despachoDetalleRepository = despachoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
        this.configuracion = configuracion;
        this.usuarioActual = usuarioActual;
    }

    /** Renglon por facturar con el origen de sus valores. */
    private record Item(String ref, TipoItem tipo, RenglonCobro cobro, Servicio servicio, Tarifa tarifa,
                        DespachoDetalle despacho, String motivoDescuento) {

        FacturaDetalleRs convertir() {
            return new FacturaDetalleRs(ref, tipo, cobro.descripcion(), cobro.cantidad(), cobro.precioUnitario(),
                    cobro.subtotal(), cobro.porcentajeDescuento(), cobro.descuento(), motivoDescuento,
                    cobro.porcentajeIva(), cobro.iva(), cobro.total());
        }
    }

    // ------------------------------------------------------------ consultas

    @Override
    @Transactional(readOnly = true)
    public List<CitaPendienteFacturaRs> citasPendientes() {
        List<Cita> citas = facturaRepository.citasPendientes();
        Set<Long> conDespacho = formulaRepository.findByCita_IdInOrderByFechaGeneracionDesc(
                        citas.stream().map(Cita::getId).toList()).stream()
                .filter(this::despachoPendiente).map(f -> f.getCita().getId()).collect(Collectors.toSet());
        return citas.stream().map(c -> new CitaPendienteFacturaRs(c.getId(), c.getFechaInicio(),
                c.getCliente().getId(), nombre(c.getCliente()), c.getCliente().getNumeroDocumento(),
                c.getMascota().getMascotaId(), c.getMascota().getNombreMascota(), nombre(c.getMedico()),
                c.getMotivo(), conDespacho.contains(c.getId()))).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaPreviaRs previsualizar(Long citaId) {
        Cita cita = citaRepository.findById(citaId).orElseThrow(() -> new NotFoundException("La cita no existe."));
        AtencionMedica atencion = exigirFacturable(cita);
        List<Item> items = items(atencion, Map.of());
        List<RenglonCobro> cobros = items.stream().map(Item::cobro).toList();
        List<String> advertencias = new ArrayList<>();
        if (formulaRepository.findByCita_IdInOrderByFechaGeneracionDesc(List.of(citaId)).stream()
                .anyMatch(this::despachoPendiente)) {
            advertencias.add("Hay medicamentos pendientes de despacho: los no despachados no se facturan.");
        }
        items.stream().filter(i -> i.tipo() == TipoItem.SERVICIO && i.tarifa() == null)
                .forEach(i -> advertencias.add("El servicio " + i.cobro().descripcion() + " no tiene tarifa vigente."));
        return new FacturaPreviaRs(citaId, cita.getFechaInicio(), cita.getCliente().getId(),
                nombre(cita.getCliente()), cita.getCliente().getNumeroDocumento(), cita.getMascota().getMascotaId(),
                cita.getMascota().getNombreMascota(), nombre(cita.getMedico()),
                cita.getMedico().getEspecializacion().getNombre(), items.stream().map(Item::convertir).toList(),
                RenglonCobro.sumar(cobros, RenglonCobro::subtotal), RenglonCobro.sumar(cobros, RenglonCobro::descuento),
                RenglonCobro.sumar(cobros, RenglonCobro::iva), RenglonCobro.sumar(cobros, RenglonCobro::total),
                tope(), advertencias);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaRs> listar(String numero, EstadoFactura estado, LocalDate desde, LocalDate hasta,
                                  Long clienteId, Integer mascotaId) {
        RangoFechas rango = RangoFechas.de(desde, hasta);
        String filtro = numero == null ? "" : Texto.normalizar(numero);
        return facturaRepository.buscar(estado, clienteId, mascotaId, rango.inicio(), rango.finExclusivo()).stream()
                .filter(f -> Texto.normalizar(f.getNumero()).contains(filtro)).map(FacturaRs::de).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaRs> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new NotFoundException("El cliente no existe.");
        }
        return listar(null, null, null, null, clienteId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaRs> listarPorMascota(Integer mascotaId) {
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new NotFoundException("La mascota no existe.");
        }
        return listar(null, null, null, null, null, mascotaId);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaRs obtener(Long id) {
        return completa(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Descarga pdf(Long id) {
        Factura factura = buscar(id);
        return FacturaPdf.generar(configuracion.obtener(), factura, detalles(id), pagos(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoRs> pagos(Long id) {
        buscar(id);
        return pagoRepository.findByFactura_IdOrderByFechaAscIdAsc(id).stream().map(PagoRs::de).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaHistorialRs> historialEstados(Long id) {
        buscar(id);
        return historialRepository.findByFactura_IdOrderByFechaAscIdAsc(id).stream()
                .map(FacturaHistorialRs::de).toList();
    }

    // ------------------------------------------------------------ operaciones

    @Override
    public FacturaRs generar(FacturaGenerarRq rq) {
        Cita cita = citaRepository.bloquear(rq.citaId())
                .orElseThrow(() -> new NotFoundException("La cita no existe."));
        AtencionMedica atencion = exigirFacturable(cita);
        formulaRepository.bloquearPorCita(cita.getId());
        List<Item> items = items(atencion, descuentos(rq.descuentos()));
        validarDescuentos(rq.descuentos(), items);
        items.stream().filter(i -> i.tipo() == TipoItem.SERVICIO && i.tarifa() == null).findFirst().ifPresent(i -> {
            throw new ConflictException("El servicio " + i.cobro().descripcion() + " no tiene tarifa vigente.");
        });
        if (items.isEmpty()) {
            throw new ConflictException("La atención no tiene nada por facturar.");
        }
        Factura factura = guardar(cita, atencion, items, rq.observacion());
        items.forEach(i -> guardar(factura, i));
        detalleRepository.flush();
        registrar(factura, null, null);
        return completa(factura);
    }

    @Override
    public FacturaRs pagar(Long id, PagoRq rq) {
        Factura factura = bloquear(id);
        if (factura.getEstado() == EstadoFactura.ANULADA || factura.getEstado() == EstadoFactura.PAGADA) {
            throw new ConflictException("La factura está " + factura.getEstado() + " y no admite pagos.");
        }
        if (rq.valor().compareTo(factura.getSaldo()) > 0) {
            throw new ConflictException("valor", "El pago supera el saldo de " + DocumentoPdf.dinero(factura.getSaldo()) + ".");
        }
        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setFecha(LocalDateTime.now());
        pago.setValor(rq.valor());
        pago.setMedioPago(rq.medioPago());
        pago.setReferencia(rq.referencia());
        pago.setUsuario(usuario());
        pagoRepository.save(pago);
        EstadoFactura anterior = factura.getEstado();
        factura.setTotalPagado(factura.getTotalPagado().add(rq.valor()));
        factura.setSaldo(factura.getTotal().subtract(factura.getTotalPagado()));
        factura.setEstado(factura.getSaldo().signum() == 0 ? EstadoFactura.PAGADA : EstadoFactura.PAGO_PARCIAL);
        facturaRepository.saveAndFlush(factura);
        registrar(factura, anterior, "Pago de " + DocumentoPdf.dinero(rq.valor()) + " (" + rq.medioPago() + ")");
        return completa(factura);
    }

    /** No borra nada: libera la cita y lo despachado para volver a facturar. Los pagos se conservan. */
    @Override
    public RespuestaRs<FacturaRs> anular(Long id, String motivo) {
        Factura factura = bloquear(id);
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new ConflictException("La factura ya está anulada.");
        }
        EstadoFactura anterior = factura.getEstado();
        for (FacturaDetalle detalle : detalleRepository.findByFactura_IdOrderById(id)) {
            detalle.setVigente(false);
            detalleRepository.save(detalle);
            if (detalle.getDespachoDetalle() != null) {
                detalle.getDespachoDetalle().setFacturado(false);
                despachoDetalleRepository.save(detalle.getDespachoDetalle());
            }
        }
        factura.setEstado(EstadoFactura.ANULADA);
        factura.setFechaAnulacion(LocalDateTime.now());
        factura.setUsuarioAnulacion(usuario());
        factura.setMotivoAnulacion(motivo.trim());
        facturaRepository.saveAndFlush(factura);
        registrar(factura, anterior, motivo.trim());
        String mensaje = factura.getTotalPagado().signum() > 0
                ? "Factura anulada. Tiene pagos por " + DocumentoPdf.dinero(factura.getTotalPagado())
                + " que se conservan y debe gestionar con el propietario." : "Factura anulada.";
        return RespuestaRs.ok(mensaje, completa(factura));
    }

    // ------------------------------------------------------------ reglas

    private AtencionMedica exigirFacturable(Cita cita) {
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw new ConflictException("Solo se facturan citas ATENDIDAS; esta se encuentra " + cita.getEstado() + ".");
        }
        if (facturaRepository.existsByCita_IdAndEstadoNot(cita.getId(), EstadoFactura.ANULADA)) {
            throw new ConflictException("citaId", "La cita ya tiene una factura vigente.");
        }
        return atencionRepository.findByCita_Id(cita.getId())
                .filter(a -> Boolean.TRUE.equals(a.getFinalizada()))
                .orElseThrow(() -> new ConflictException("La cita no tiene una atención finalizada."));
    }

    /** Servicios con su tarifa vigente y medicamentos dispensados con el precio congelado en el despacho. */
    private List<Item> items(AtencionMedica atencion, Map<String, FacturaGenerarRq.Descuento> descuentos) {
        List<Item> items = new ArrayList<>();
        for (AtencionServicio s : atencionServicioRepository.findByAtencion_IdOrderById(atencion.getId())) {
            Tarifa tarifa = tarifaRepository.findByServicio_IdAndActivaTrue(s.getServicio().getId()).orElse(null);
            String ref = "SERVICIO-" + s.getServicio().getId();
            FacturaGenerarRq.Descuento descuento = descuentos.get(ref);
            items.add(new Item(ref, TipoItem.SERVICIO, RenglonCobro.de("Servicio", s.getServicio().getNombre(),
                    s.getCantidad(), tarifa == null ? BigDecimal.ZERO : tarifa.getValor(), porcentaje(descuento),
                    tarifa == null ? BigDecimal.ZERO : tarifa.getImpuesto().getPorcentaje()), s.getServicio(), tarifa,
                    null, motivo(descuento)));
        }
        for (DespachoDetalle d : despachoDetalleRepository.porFacturar(atencion.getCita().getId())) {
            String ref = "MEDICAMENTO-" + d.getId();
            FacturaGenerarRq.Descuento descuento = descuentos.get(ref);
            items.add(new Item(ref, TipoItem.MEDICAMENTO, RenglonCobro.de("Medicamento",
                    d.getMedicamento().getNombre(), d.getCantidadEntregada(), d.getPrecioVentaUnitario(),
                    porcentaje(descuento), d.getPorcentajeIva()), null, null, d, motivo(descuento)));
        }
        return items;
    }

    private static Map<String, FacturaGenerarRq.Descuento> descuentos(List<FacturaGenerarRq.Descuento> lista) {
        Map<String, FacturaGenerarRq.Descuento> mapa = new HashMap<>();
        if (lista != null) {
            lista.forEach(d -> {
                if (mapa.put(d.itemRef(), d) != null) {
                    throw new ConflictException("descuentos", "El renglón " + d.itemRef() + " tiene dos descuentos.");
                }
            });
        }
        return mapa;
    }

    private void validarDescuentos(List<FacturaGenerarRq.Descuento> descuentos, List<Item> items) {
        if (descuentos == null) {
            return;
        }
        Set<String> refs = items.stream().map(Item::ref).collect(Collectors.toSet());
        BigDecimal tope = tope();
        List<ErrorResponse.Detalle> errores = new ArrayList<>();
        for (int i = 0; i < descuentos.size(); i++) {
            FacturaGenerarRq.Descuento d = descuentos.get(i);
            if (!refs.contains(d.itemRef())) {
                errores.add(new ErrorResponse.Detalle("descuentos[" + i + "].itemRef",
                        "El renglón " + d.itemRef() + " no está en la factura."));
            } else if (d.porcentaje().compareTo(tope) > 0) {
                errores.add(new ErrorResponse.Detalle("descuentos[" + i + "].porcentaje",
                        "Su tope de descuento es " + DocumentoPdf.numero(tope) + " %."));
            }
        }
        if (!errores.isEmpty()) {
            throw new ConflictException(errores.stream().map(ErrorResponse.Detalle::mensaje)
                    .collect(Collectors.joining(" ")), errores);
        }
    }

    private BigDecimal tope() {
        return Rol.ADMINISTRADOR.equals(usuarioActual.obtener().rol()) ? BigDecimal.valueOf(100)
                : configuracion.obtener().topeDescuentoSecretaria();
    }

    private boolean despachoPendiente(FormulaMedica f) {
        return f.getEstadoFormula() == EstadoFormula.BORRADOR
                || f.getEstadoDespacho() != null && DESPACHO_PENDIENTE.contains(f.getEstadoDespacho());
    }

    // ------------------------------------------------------------ guardado

    private Factura guardar(Cita cita, AtencionMedica atencion, List<Item> items, String observacion) {
        List<RenglonCobro> cobros = items.stream().map(Item::cobro).toList();
        Factura f = new Factura();
        f.setNumero(configuracion.siguienteFactura());
        f.setCita(cita);
        f.setAtencion(atencion);
        f.setCliente(cita.getCliente());
        f.setMascota(cita.getMascota());
        f.setMedico(cita.getMedico());
        f.setClienteNombre(nombre(cita.getCliente()));
        f.setClienteDocumento(cita.getCliente().getTipoDocumento() + " " + cita.getCliente().getNumeroDocumento());
        f.setMascotaNombre(cita.getMascota().getNombreMascota());
        f.setMedicoNombre(nombre(cita.getMedico()));
        f.setEspecialidadNombre(cita.getMedico().getEspecializacion().getNombre());
        f.setFechaGeneracion(LocalDateTime.now());
        f.setSubtotal(RenglonCobro.sumar(cobros, RenglonCobro::subtotal));
        f.setTotalDescuento(RenglonCobro.sumar(cobros, RenglonCobro::descuento));
        f.setTotalImpuestos(RenglonCobro.sumar(cobros, RenglonCobro::iva));
        f.setTotal(RenglonCobro.sumar(cobros, RenglonCobro::total));
        f.setTotalPagado(BigDecimal.ZERO.setScale(2));
        f.setSaldo(f.getTotal());
        // Con descuento total no queda nada por pagar.
        f.setEstado(f.getTotal().signum() == 0 ? EstadoFactura.PAGADA : EstadoFactura.PENDIENTE);
        f.setObservacion(observacion);
        f.setUsuario(usuario());
        return facturaRepository.saveAndFlush(f);
    }

    private void guardar(Factura factura, Item item) {
        RenglonCobro c = item.cobro();
        FacturaDetalle d = new FacturaDetalle();
        d.setFactura(factura);
        d.setTipoItem(item.tipo());
        d.setServicio(item.servicio());
        d.setTarifa(item.tarifa());
        d.setDescripcion(c.descripcion());
        d.setCantidad(c.cantidad());
        d.setPrecioUnitario(c.precioUnitario());
        d.setPorcentajeDescuento(c.porcentajeDescuento());
        d.setValorDescuento(c.descuento());
        d.setMotivoDescuento(item.motivoDescuento());
        d.setPorcentajeIva(c.porcentajeIva());
        d.setValorIva(c.iva());
        d.setSubtotal(c.subtotal());
        d.setTotal(c.total());
        if (item.despacho() != null) {
            d.setDespachoDetalle(item.despacho());
            d.setMedicamento(item.despacho().getMedicamento());
            item.despacho().setFacturado(true);
            despachoDetalleRepository.save(item.despacho());
        }
        detalleRepository.save(d);
    }

    private void registrar(Factura factura, EstadoFactura anterior, String motivo) {
        FacturaHistorialEstado h = new FacturaHistorialEstado();
        h.setFactura(factura);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(factura.getEstado());
        h.setMotivo(motivo);
        h.setUsuario(usuario());
        h.setFecha(LocalDateTime.now());
        historialRepository.save(h);
    }

    private FacturaRs completa(Factura factura) {
        return FacturaRs.de(factura, detalles(factura.getId()), pagoRepository
                .findByFactura_IdOrderByFechaAscIdAsc(factura.getId()).stream().map(PagoRs::de).toList());
    }

    private List<FacturaDetalleRs> detalles(Long facturaId) {
        return detalleRepository.findByFactura_IdOrderById(facturaId).stream().map(FacturaDetalleRs::de).toList();
    }

    private Factura buscar(Long id) {
        return facturaRepository.findById(id).orElseThrow(() -> new NotFoundException("La factura no existe."));
    }

    private Factura bloquear(Long id) {
        return facturaRepository.bloquear(id).orElseThrow(() -> new NotFoundException("La factura no existe."));
    }

    private Usuario usuario() {
        return usuarioRepository.findById(usuarioActual.obtener().id()).orElseThrow();
    }

    private static BigDecimal porcentaje(FacturaGenerarRq.Descuento d) {
        return d == null ? BigDecimal.ZERO : d.porcentaje();
    }

    private static String motivo(FacturaGenerarRq.Descuento d) {
        return d == null ? null : d.motivo().trim();
    }

    private static String nombre(Cliente c) {
        return c.getNombres() + " " + c.getApellidos();
    }

    private static String nombre(Medico m) {
        return m.getNombres() + " " + m.getApellidos();
    }
}
