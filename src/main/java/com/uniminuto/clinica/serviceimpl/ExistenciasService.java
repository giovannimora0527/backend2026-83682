package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CargaInventario;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import com.uniminuto.clinica.entity.enums.TipoProducto;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unico punto que cambia existencias y registra movimientos. Debe llamarse dentro de una transaccion y con el
 * producto bloqueado, para que los saldos del movimiento sean exactos.
 */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class ExistenciasService {

    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    public ExistenciasService(MedicamentoRepository medicamentoRepository, LoteMedicamentoRepository loteRepository,
                              MovimientoInventarioRepository movimientoRepository,
                              UsuarioRepository usuarioRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Medicamento bloquearProducto(Long id) {
        return medicamentoRepository.bloquear(id)
                .orElseThrow(() -> new NotFoundException("El producto no existe."));
    }

    /** Crea el lote y su movimiento de entrada. */
    public LoteMedicamento ingresar(Medicamento producto, LoteRq rq, TipoMovimiento tipo, CargaInventario carga,
                                    Long usuarioId) {
        validarLote(producto, rq);
        LoteMedicamento lote = new LoteMedicamento();
        lote.setMedicamento(producto);
        lote.setNumeroLote(rq.numeroLote().trim());
        lote.setPrecioCompra(rq.precioCompra());
        lote.setCantidadInicial(rq.cantidad());
        lote.setCantidadDisponible(rq.cantidad());
        lote.setFechaCompra(rq.fechaCompra());
        lote.setFechaIngreso(rq.fechaIngreso() == null ? LocalDate.now() : rq.fechaIngreso());
        lote.setFechaVencimiento(rq.fechaVencimiento());
        lote = loteRepository.saveAndFlush(lote);
        MovimientoInventario movimiento = nuevo(tipo, lote, rq.cantidad(), usuarioId);
        movimiento.setCarga(carga);
        movimiento.setObservacion(rq.observacion());
        movimientoRepository.save(movimiento);
        return lote;
    }

    public MovimientoInventario ajustar(LoteMedicamento lote, TipoMovimiento tipo, int cantidad, String motivo,
                                        Long usuarioId) {
        if (tipo == TipoMovimiento.AJUSTE_POSITIVO) {
            lote.setCantidadDisponible(Math.addExact(lote.getCantidadDisponible(), cantidad));
            // Lo disponible nunca supera lo ingresado: un sobrante corrige tambien la cantidad inicial.
            lote.setCantidadInicial(Math.max(lote.getCantidadInicial(), lote.getCantidadDisponible()));
        } else if (tipo == TipoMovimiento.AJUSTE_NEGATIVO) {
            if (cantidad > lote.getCantidadDisponible()) {
                throw new ConflictException("cantidad", "El lote solo tiene " + lote.getCantidadDisponible()
                        + " unidades disponibles.");
            }
            lote.setCantidadDisponible(lote.getCantidadDisponible() - cantidad);
        } else {
            throw new BadRequestException("El tipo de ajuste debe ser AJUSTE_POSITIVO o AJUSTE_NEGATIVO.");
        }
        loteRepository.saveAndFlush(lote);
        MovimientoInventario movimiento = nuevo(tipo, lote, cantidad, usuarioId);
        movimiento.setObservacion(motivo.trim());
        return movimientoRepository.save(movimiento);
    }

    private void validarLote(Medicamento producto, LoteRq rq) {
        LocalDate hoy = LocalDate.now();
        if (rq.fechaCompra().isAfter(hoy)) {
            throw new BadRequestException("La fecha de compra no puede ser futura.");
        }
        if (rq.fechaIngreso() != null && (rq.fechaIngreso().isAfter(hoy)
                || rq.fechaIngreso().isBefore(rq.fechaCompra()))) {
            throw new BadRequestException("La fecha de ingreso debe estar entre la fecha de compra y hoy.");
        }
        if (rq.fechaVencimiento() == null) {
            if (producto.getTipoProducto() == TipoProducto.MEDICAMENTO) {
                throw new BadRequestException("Los medicamentos exigen fecha de vencimiento.");
            }
        } else if (!rq.fechaVencimiento().isAfter(rq.fechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la de compra.");
        } else if (rq.fechaVencimiento().isBefore(hoy)) {
            throw new ConflictException("fechaVencimiento", "No se reciben lotes vencidos.");
        }
        if (loteRepository.existsByMedicamento_IdAndNumeroLoteIgnoreCase(producto.getId(), rq.numeroLote().trim())) {
            throw new ConflictException("numeroLote", "El producto ya tiene un lote con ese número.");
        }
    }

    private MovimientoInventario nuevo(TipoMovimiento tipo, LoteMedicamento lote, int cantidad, Long usuarioId) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setTipo(tipo);
        movimiento.setMedicamento(lote.getMedicamento());
        movimiento.setLote(lote);
        movimiento.setCantidad(cantidad);
        movimiento.setSaldoLoteResultante(lote.getCantidadDisponible());
        movimiento.setSaldoProductoResultante(
                Math.toIntExact(loteRepository.existencias(lote.getMedicamento().getId())));
        movimiento.setCostoUnitario(lote.getPrecioCompra());
        movimiento.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        return movimiento;
    }
}
