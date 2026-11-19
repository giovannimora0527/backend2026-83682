package com.uniminuto.clinica.inventario;

import static com.uniminuto.clinica.inventario.InventarioDatos.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventarioServicio {
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private final InventarioRepositorio datos;

    public InventarioServicio(InventarioRepositorio datos) {
        this.datos = datos;
    }

    public List<Ficha> catalogo() {
        return datos.catalogo(LocalDate.now(BOGOTA),null);
    }

    @Transactional
    public Ficha guardar(Long id, Producto producto) {
        Producto p=validar(producto);
        try {
            if (id==null) {
                id=datos.crear(p,ahora());
            } else {
                datos.bloquearProducto(id);
                datos.editar(id,p,ahora());
            }
        } catch (DuplicateKeyException e) {
            throw conflicto("Ya existe un producto con ese código o nombre.");
        }
        return datos.catalogo(LocalDate.now(BOGOTA),id).getFirst();
    }

    public List<Lote> lotes(long producto, boolean disponibles) {
        var lista=datos.lotes(producto,LocalDate.now(BOGOTA));
        return disponibles ? lista.stream().filter(Lote::disponible).toList() : lista;
    }

    public List<Movimiento> movimientos(long producto) {
        return datos.movimientos(producto);
    }

    @Transactional
    public long registrarLote(long producto, RegistrarLote solicitud, long usuario) {
        boolean activo=datos.bloquearProducto(producto);
        if (!activo) throw conflicto("El producto está inactivo.");
        RegistrarLote lote=validarLote(solicitud);
        long id;
        try {
            id=datos.crearLote(producto,lote);
        } catch (DuplicateKeyException e) {
            throw conflicto("Ya existe ese número de lote para el producto.");
        }
        if (lote.cantidad()>0) {
            datos.movimiento(id,usuario,new RegistrarMovimiento(TipoMovimiento.ENTRADA,lote.cantidad(),
                    "Ingreso inicial de lote",lote.referencia()),lote.cantidad(),ahora());
        }
        return id;
    }

    @Transactional
    public int registrarMovimiento(long producto, long loteId, RegistrarMovimiento solicitud, long usuario) {
        return aplicarMovimiento(producto,loteId,solicitud,usuario,false).saldo();
    }

    public record MovimientoAplicado(long id, int saldo) {}

    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
    public List<Lote> lotesBloqueadosParaDespacho(long producto) {
        datos.bloquearProducto(producto);
        return lotes(producto,true);
    }

    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.MANDATORY)
    public MovimientoAplicado movimientoDespacho(long producto,long lote,int cantidad,long usuario,
            long formula,long entrega,boolean reversion) {
        var solicitud=new RegistrarMovimiento(reversion?TipoMovimiento.ENTRADA:TipoMovimiento.SALIDA,
                cantidad,reversion?"Reversión de entrega":"Dispensación confirmada","Fórmula "+formula+" / entrega "+entrega);
        return aplicarMovimiento(producto,lote,solicitud,usuario,reversion);
    }

    private MovimientoAplicado aplicarMovimiento(long producto,long loteId,RegistrarMovimiento solicitud,long usuario,
            boolean devolverAlLoteOriginal) {
        if (solicitud==null || solicitud.tipo()==null || solicitud.cantidad()==null
                || solicitud.cantidad()==0 || (solicitud.tipo()!=TipoMovimiento.AJUSTE && solicitud.cantidad()<0)) {
            throw invalido("Indica un tipo y una cantidad válida. El ajuste admite valores positivos y negativos.");
        }
        String motivo=texto(solicitud.motivo(),255,true,"motivo");
        String referencia=texto(solicitud.referencia(),100,false,"referencia");
        boolean activo=datos.bloquearProducto(producto);
        Lote lote=datos.bloquearLote(producto,loteId,LocalDate.now(BOGOTA),activo);
        long saldo=(long)lote.cantidad()+variacion(solicitud.tipo(),solicitud.cantidad());
        if (saldo<0 || saldo>Integer.MAX_VALUE) throw conflicto("El movimiento dejaría un saldo inválido.");
        if (solicitud.tipo()==TipoMovimiento.SALIDA && !lote.disponible()) {
            throw conflicto("El lote está vencido, agotado o pertenece a un producto inactivo. Usa un ajuste justificado para bajas.");
        }
        if (!activo && saldo>lote.cantidad() && !devolverAlLoteOriginal) throw conflicto("El producto está inactivo.");
        long movimiento=datos.movimiento(loteId,usuario,new RegistrarMovimiento(solicitud.tipo(),solicitud.cantidad(),motivo,referencia),
                (int)saldo,ahora());
        return new MovimientoAplicado(movimiento,(int)saldo);
    }

    long importar(Producto p, RegistrarLote lote, long usuario) {
        long id=datos.crear(p,ahora());
        if (lote!=null) registrarLote(id,lote,usuario);
        return id;
    }

    private static long variacion(TipoMovimiento tipo, int cantidad) {
        return tipo==TipoMovimiento.SALIDA ? -(long)cantidad : cantidad;
    }

    static Producto validar(Producto p) {
        if (p==null) throw invalido("Faltan los datos del producto.");
        String codigo=texto(p.codigo(),8,true,"código");
        if (!codigo.matches("[A-Za-z0-9]{8}")) throw invalido("El código debe tener exactamente 8 caracteres alfanuméricos.");
        if (p.tipo()==null) throw invalido("Selecciona un tipo de producto.");
        if (p.fechaCompra()==null || p.fechaIngreso()==null || p.fechaIngreso().isBefore(p.fechaCompra())) {
            throw invalido("La fecha de ingreso debe ser igual o posterior a la fecha de compra.");
        }
        fechas(p.fechaIngreso(),p.fechaVence(),"La fecha de vencimiento debe ser igual o posterior al ingreso.");
        fechas(p.fechaIngreso(),p.fechaProximoPedido(),"La fecha de próximo pedido debe ser igual o posterior al ingreso.");
        if (p.stockMinimo()==null || p.stockMinimo()<0) throw invalido("El stock mínimo debe ser un entero no negativo.");
        return new Producto(codigo.toUpperCase(Locale.ROOT),texto(p.nombre(),100,true,"nombre"),texto(p.descripcion(),5000,false,"descripción"),
                texto(p.presentacion(),100,false,"presentación"),p.tipo(),
                texto(p.invima(),100,p.tipo()==TipoProducto.MEDICAMENTO,"registro INVIMA"),
                precio(p.precioCompra(),"compra"),precio(p.precioVenta(),"venta"),p.stockMinimo(),
                p.fechaCompra(),p.fechaIngreso(),p.fechaProximoPedido(),p.fechaVence(),!Boolean.FALSE.equals(p.activo()));
    }

    static RegistrarLote validarLote(RegistrarLote lote) {
        if (lote==null || lote.fechaIngreso()==null || lote.cantidad()==null || lote.cantidad()<0) {
            throw invalido("El lote requiere fecha de ingreso y cantidad entera no negativa.");
        }
        fechas(lote.fechaIngreso(),lote.fechaVence(),"El vencimiento del lote debe ser igual o posterior al ingreso.");
        return new RegistrarLote(texto(lote.numero(),60,true,"número de lote"),precio(lote.costo(),"lote"),
                lote.fechaIngreso(),lote.fechaVence(),lote.cantidad(),texto(lote.referencia(),100,false,"referencia"));
    }

    private static void fechas(LocalDate ingreso, LocalDate vence, String mensaje) {
        if (vence!=null && vence.isBefore(ingreso)) throw invalido(mensaje);
    }

    private static BigDecimal precio(BigDecimal valor, String nombre) {
        if (valor==null || valor.signum()<0 || valor.compareTo(new BigDecimal("999999999999.99"))>0
                || valor.stripTrailingZeros().scale()>2) {
            throw invalido("El precio de "+nombre+" debe ser no negativo y admitir hasta dos decimales.");
        }
        return valor;
    }

    static String texto(String valor, int limite, boolean obligatorio, String nombre) {
        String limpio=valor==null ? "" : valor.strip();
        if (limpio.length()>limite || (obligatorio && limpio.isEmpty())
                || limpio.chars().anyMatch(c->Character.isISOControl(c) && c!='\n' && c!='\r' && c!='\t')) {
            throw invalido("Campo inválido: "+nombre+".");
        }
        return limpio.isEmpty() ? null : limpio;
    }

    static LocalDateTime ahora() { return LocalDateTime.now(BOGOTA); }

    static ResponseStatusException invalido(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,mensaje);
    }

    static ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT,mensaje);
    }
}
