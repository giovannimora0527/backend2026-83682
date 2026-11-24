package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Impuesto;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.TarifaHistorial;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.TarifaActualizarRq;
import com.uniminuto.clinica.models.TarifaCrearRq;
import com.uniminuto.clinica.models.TarifaHistorialRs;
import com.uniminuto.clinica.models.TarifaRs;
import com.uniminuto.clinica.repository.ImpuestoRepository;
import com.uniminuto.clinica.repository.ServicioRepository;
import com.uniminuto.clinica.repository.TarifaHistorialRepository;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.TarifaService;
import com.uniminuto.clinica.util.RangoFechas;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TarifaServiceImpl implements TarifaService {

    private final TarifaRepository tarifaRepository;
    private final TarifaHistorialRepository historialRepository;
    private final ServicioRepository servicioRepository;
    private final ImpuestoRepository impuestoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;

    public TarifaServiceImpl(TarifaRepository tarifaRepository, TarifaHistorialRepository historialRepository,
                             ServicioRepository servicioRepository, ImpuestoRepository impuestoRepository,
                             UsuarioRepository usuarioRepository, UsuarioActual usuarioActual) {
        this.tarifaRepository = tarifaRepository;
        this.historialRepository = historialRepository;
        this.servicioRepository = servicioRepository;
        this.impuestoRepository = impuestoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActual = usuarioActual;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarifaRs> listar(Long servicioId, boolean soloVigentes) {
        return tarifaRepository.buscar(servicioId, soloVigentes).stream().map(TarifaRs::de).toList();
    }

    @Override
    @Transactional
    public TarifaRs crear(TarifaCrearRq rq) {
        Servicio servicio = servicioRepository.findById(rq.servicioId())
                .orElseThrow(() -> new NotFoundException("El servicio no existe."));
        if (rq.vigenteDesde() != null && rq.vigenteDesde().isAfter(LocalDate.now())) {
            throw new BadRequestException("La vigencia no puede iniciar en el futuro.");
        }
        if (tarifaRepository.findByServicio_IdAndActivaTrue(servicio.getId()).isPresent()) {
            throw new ConflictException("servicioId", "El servicio ya tiene una tarifa vigente; actualícela.");
        }
        Tarifa tarifa = nueva(servicio, rq.valor(), impuesto(rq.impuestoId()),
                rq.vigenteDesde() == null ? LocalDate.now() : rq.vigenteDesde());
        registrar(tarifa, null, "Creación de la tarifa");
        return TarifaRs.de(tarifa);
    }

    @Override
    @Transactional
    public TarifaRs actualizar(Long id, TarifaActualizarRq rq) {
        Tarifa anterior = tarifaRepository.bloquear(id)
                .orElseThrow(() -> new NotFoundException("La tarifa no existe."));
        if (!Boolean.TRUE.equals(anterior.getActiva())) {
            throw new ConflictException("La tarifa ya no está vigente.");
        }
        Impuesto impuesto = impuesto(rq.impuestoId());
        if (anterior.getValor().compareTo(rq.valor()) == 0 && anterior.getImpuesto().getId().equals(impuesto.getId())) {
            throw new ConflictException("La tarifa no tiene cambios.");
        }
        LocalDate hoy = LocalDate.now();
        anterior.setActiva(false);
        anterior.setVigenteHasta(anterior.getVigenteDesde().isAfter(hoy) ? anterior.getVigenteDesde() : hoy);
        tarifaRepository.saveAndFlush(anterior);
        Tarifa tarifa = nueva(anterior.getServicio(), rq.valor(), impuesto, hoy);
        registrar(tarifa, anterior, rq.motivo().trim());
        return TarifaRs.de(tarifa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarifaHistorialRs> historial(Long tarifaId) {
        Tarifa tarifa = tarifaRepository.findById(tarifaId)
                .orElseThrow(() -> new NotFoundException("La tarifa no existe."));
        return historialRepository.findByServicio_IdOrderByFechaDescIdDesc(tarifa.getServicio().getId()).stream()
                .map(TarifaHistorialRs::de).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TarifaHistorialRs> historial(LocalDate desde, LocalDate hasta) {
        RangoFechas rango = RangoFechas.de(desde, hasta);
        return historialRepository.findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaDescIdDesc(
                rango.inicio(), rango.finExclusivo()).stream().map(TarifaHistorialRs::de).toList();
    }

    private Tarifa nueva(Servicio servicio, BigDecimal valor, Impuesto impuesto, LocalDate desde) {
        Tarifa tarifa = new Tarifa();
        tarifa.setServicio(servicio);
        tarifa.setValor(valor);
        tarifa.setImpuesto(impuesto);
        tarifa.setVigenteDesde(desde);
        tarifa.setActiva(true);
        return tarifaRepository.saveAndFlush(tarifa);
    }

    private Impuesto impuesto(Long id) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El impuesto no existe."));
        if (!Boolean.TRUE.equals(impuesto.getActivo())) {
            throw new ConflictException("impuestoId", "El impuesto está inactivo.");
        }
        return impuesto;
    }

    private void registrar(Tarifa tarifa, Tarifa anterior, String motivo) {
        TarifaHistorial h = new TarifaHistorial();
        h.setTarifa(tarifa);
        h.setServicio(tarifa.getServicio());
        h.setValorAnterior(anterior == null ? null : anterior.getValor());
        h.setValorNuevo(tarifa.getValor());
        h.setPorcentajeIvaAnterior(anterior == null ? null : anterior.getImpuesto().getPorcentaje());
        h.setPorcentajeIvaNuevo(tarifa.getImpuesto().getPorcentaje());
        h.setMotivo(motivo);
        h.setUsuario(usuarioRepository.getReferenceById(usuarioActual.obtener().id()));
        h.setFecha(LocalDateTime.now());
        historialRepository.save(h);
    }
}
