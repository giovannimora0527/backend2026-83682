package com.uniminuto.clinica.carga;

import com.uniminuto.clinica.entity.CargaInventarioFila;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.enums.TipoMovimiento;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.LoteRq;
import com.uniminuto.clinica.repository.CargaInventarioFilaRepository;
import com.uniminuto.clinica.repository.ImpuestoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.serviceimpl.ExistenciasService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** Cada fila se importa en su propia transaccion: el fallo de una no afecta a las demas. */
@Component
public class ImportadorFila {

    private final CargaInventarioFilaRepository filaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ImpuestoRepository impuestoRepository;
    private final ValidadorCarga validador;
    private final ExistenciasService existencias;
    private final EntityManager entityManager;

    public ImportadorFila(CargaInventarioFilaRepository filaRepository, MedicamentoRepository medicamentoRepository,
                          ImpuestoRepository impuestoRepository, ValidadorCarga validador,
                          ExistenciasService existencias, EntityManager entityManager) {
        this.entityManager = entityManager;
        this.filaRepository = filaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.impuestoRepository = impuestoRepository;
        this.validador = validador;
        this.existencias = existencias;
    }

    /** Revalida contra la base, que pudo cambiar desde la vista previa. */
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void importar(Long filaId, Long usuarioId) {
        entityManager.clear();
        CargaInventarioFila fila = filaRepository.findById(filaId).orElseThrow();
        ValidadorCarga.Resultado resultado = validador.validar(List.of(fila.getDatosJson())).getFirst();
        if (!resultado.valida()) {
            throw new ConflictException(resultado.errores().stream()
                    .map(e -> e.campo() + ": " + e.mensaje()).collect(Collectors.joining("; ")));
        }
        DatosFila d = resultado.datos();
        Medicamento producto = medicamentoRepository.findByCodigo(d.codigo()).orElseGet(() -> crear(d));
        producto = existencias.bloquearProducto(producto.getId());
        LoteMedicamento lote = existencias.ingresar(producto,
                new LoteRq(d.numeroLote(), d.precioCompra(), d.cantidad(), d.fechaCompra(), null,
                        d.fechaVencimiento(), "Carga masiva, fila " + fila.getNumeroFila()),
                TipoMovimiento.ENTRADA_CARGA_MASIVA, fila.getCarga(), usuarioId);
        fila.setMedicamento(producto);
        fila.setLote(lote);
        fila.setImportada(true);
        fila.setErrorImportacion(null);
        filaRepository.save(fila);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarError(Long filaId, String mensaje) {
        // La fila fallida pudo dejar entidades a medias en el contexto compartido de la peticion.
        entityManager.clear();
        CargaInventarioFila fila = filaRepository.findById(filaId).orElseThrow();
        fila.setErrorImportacion(mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje);
        filaRepository.save(fila);
    }

    private Medicamento crear(DatosFila d) {
        Medicamento producto = new Medicamento();
        producto.setCodigo(d.codigo());
        producto.setNombre(d.nombre());
        producto.setTipoProducto(d.tipo());
        producto.setDescripcion(d.descripcion());
        producto.setPresentacion(d.presentacion());
        producto.setUnidadMedida(d.unidadMedida());
        producto.setRequiereInvima(d.requiereInvima());
        producto.setRegistroInvima(d.registroInvima());
        producto.setPrecioVenta(d.precioVenta());
        producto.setImpuesto(impuestoRepository.findByActivoTrueOrderByPorcentaje().stream()
                .filter(i -> i.getPorcentaje().compareTo(d.porcentajeIva()) == 0).findFirst().orElseThrow());
        producto.setStockMinimo(d.stockMinimo());
        producto.setFechaProximoPedido(d.fechaProximoPedido());
        producto.setFechaCreacionRegistro(LocalDateTime.now());
        return medicamentoRepository.saveAndFlush(producto);
    }
}
