package com.uniminuto.clinica.facturacion;

import static com.uniminuto.clinica.calendario.AgendaServicio.*;
import static com.uniminuto.clinica.facturacion.FacturaRepositorio.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
class FacturaServicio {
    private static final Set<String> ESTADOS=Set.of("PENDIENTE_PAGO","PAGO_PARCIAL","PAGADA","ANULADA");
    private static final Set<String> METODOS=Set.of("EFECTIVO","TARJETA","TRANSFERENCIA","OTRO");
    private static final BigDecimal MAXIMO=new BigDecimal("99999999999999999.99");
    private final FacturaRepositorio repo;

    record Emitir(UUID clave,String previa,BigDecimal descuento,String motivo) {}
    record Pago(UUID clave,BigDecimal importe,BigDecimal saldoEsperado,String metodo,String referencia) {}
    record Anular(String motivo) {}
    record Detalle(Map<String,Object> factura,List<Map<String,Object>> lineas,List<Map<String,Object>> pagos,
                    List<Map<String,Object>> historial) {}

    FacturaServicio(FacturaRepositorio repo) {
        this.repo=repo;
    }

    List<Origen> elegibles(int pagina) {
        financiero();
        return repo.elegibles(offset(pagina));
    }

    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    Previa previa(long id, BigDecimal descuento, String motivo) {
        var p=descuento(descuento,motivo);
        return calcular(repo.origen(id,false),p,false);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Detalle emitir(long id,Emitir solicitud) {
        if(solicitud==null || solicitud.clave()==null || solicitud.previa()==null
                || !solicitud.previa().matches("[a-f0-9]{64}")) throw invalido("Revisa la vista previa antes de emitir.");
        var actor=financiero();
        var rebaja=descuento(solicitud.descuento(),solicitud.motivo());
        var clave=solicitud.clave().toString();
        var huella=hash(id+"|"+solicitud.previa()+"|"+rebaja.tasa()+"|"+rebaja.motivo());
        var o=repo.origen(id,true);
        var existente=repo.porClave(clave);
        if(existente!=null) {
            if(!MessageDigest.isEqual(huella,(byte[])existente.get("huella"))) {
                throw conflicto("La clave de emisión ya se usó con otros datos.");
            }
            return consultar(((Number)existente.get("id")).longValue());
        }
        var previa=calcular(o,rebaja,true);
        if(!previa.huella().equals(solicitud.previa())) throw conflicto("Cambió la factura. Actualiza la vista previa.");
        String estado=previa.importes().total().signum()==0?"PAGADA":"PENDIENTE_PAGO";
        long factura;
        try {
            factura=repo.emitir(previa,clave,huella,actor.usuarioId(),"FV-"+clave.toUpperCase(java.util.Locale.ROOT),
                    rebaja.tasa(),rebaja.motivo(),estado);
        } catch(DuplicateKeyException e) {
            throw conflicto("Ya se facturó un concepto o se utilizó la clave. Actualiza la información.");
        }
        return consultar(factura);
    }

    private Previa calcular(Origen o, Rebaja descuento, boolean bloqueo) {
        if(!o.finalizada()) throw conflicto("La atención y su cita deben estar finalizadas.");
        try {
            var conceptos=repo.conceptos(o.id(),bloqueo);
            if(conceptos.isEmpty()) throw conflicto("Esta atención no tiene conceptos pendientes de facturar.");
            var lineas=conceptos.stream().map(c->new Linea(c,
                    ImportesFactura.linea(c.cantidad(),c.precio(),descuento.tasa(),c.iva(),c.otros()))).toList();
            String huella=HexFormat.of().formatHex(hash(o+"|"+lineas+"|"+descuento));
            return new Previa(o,lineas,ImportesFactura.sumar(lineas.stream().map(Linea::importes).toList()),huella);
        } catch(IllegalArgumentException e) {
            throw invalido(e.getMessage());
        }
    }

    @Transactional(readOnly=true)
    Detalle consultar(long id) {
        financiero();
        return new Detalle(decimales(repo.factura(id)),repo.lineas(id).stream().map(FacturaServicio::decimales).toList(),
                repo.pagos(id).stream().map(FacturaServicio::decimales).toList(),repo.historial(id));
    }

    List<Map<String,Object>> listar(LocalDate desde,LocalDate hasta,String propietario,String mascota,
                                   Long propietarioId,Long mascotaId,String estado,int pagina) {
        financiero();
        if(desde!=null && hasta!=null && desde.isAfter(hasta) || hasta!=null && hasta.getYear()>9998) {
            throw invalido("Revisa el rango de fechas.");
        }
        if(estado!=null && !ESTADOS.contains(estado)) throw invalido("Estado inválido.");
        return repo.listar(desde,hasta==null?null:hasta.plusDays(1),texto(propietario,255,false),texto(mascota,160,false),
                propietarioId,mascotaId,estado,offset(pagina)).stream().map(FacturaServicio::decimales).toList();
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Detalle pago(long id,Pago solicitud) {
        var actor=financiero();
        if(solicitud==null || solicitud.clave()==null) throw invalido("Falta la clave del pago.");
        var importe=importe(solicitud.importe());
        if(importe.signum()==0) throw invalido("El pago debe ser mayor que cero.");
        var esperado=importe(solicitud.saldoEsperado());
        var metodo=texto(solicitud.metodo(),32,true);
        if(!METODOS.contains(metodo)) throw invalido("Selecciona un método de pago válido.");
        var referencia=texto(solicitud.referencia(),100,false);
        var f=repo.bloquear(id);
        String clave=solicitud.clave().toString();
        var previo=repo.pagoPorClave(id,clave);
        if(previo!=null) {
            if(importe.compareTo((BigDecimal)previo.get("importe"))!=0 || !metodo.equals(previo.get("metodo"))
                    || !Objects.equals(referencia,previo.get("referencia"))) {
                throw conflicto("La clave del pago ya se usó con otro importe, método o referencia.");
            }
            return consultar(id);
        }
        if("ANULADA".equals(f.estado())) throw conflicto("La factura está anulada.");
        var saldo=f.total().subtract(f.pagado());
        if(saldo.compareTo(esperado)!=0) throw conflicto("El saldo cambió. Actualiza la factura antes de registrar el pago.");
        if(importe.compareTo(saldo)>0) throw conflicto("El pago supera el saldo pendiente.");
        repo.pagar(f,actor.usuarioId(),clave,importe,metodo,referencia);
        return consultar(id);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Detalle anular(long id,Anular solicitud) {
        var actor=financiero();
        if(actor.rol()!=Rol.ADMIN) throw prohibido("Solo Administración puede anular una factura.");
        var motivo=texto(solicitud==null?null:solicitud.motivo(),255,true);
        var f=repo.bloquear(id);
        if("ANULADA".equals(f.estado())) return consultar(id);
        if(f.pagado().signum()!=0) throw conflicto("La factura tiene pagos registrados y requiere un procedimiento de devolución.");
        repo.anular(f,actor.usuarioId(),motivo);
        return consultar(id);
    }

    private record Rebaja(BigDecimal tasa,String motivo) {}

    private static Rebaja descuento(BigDecimal tasa,String motivo) {
        var actor=financiero();
        var valor=importe(tasa==null?BigDecimal.ZERO:tasa);
        if(valor.compareTo(BigDecimal.valueOf(100))>0) throw invalido("El descuento no puede superar el 100 %.");
        if(valor.signum()>0 && actor.rol()!=Rol.ADMIN) throw prohibido("Solo Administración puede aplicar descuentos.");
        return new Rebaja(valor,valor.signum()==0?null:texto(motivo,255,true));
    }

    private static Map<String,Object> decimales(Map<String,Object> fila) {
        Map<String,Object> salida=new java.util.LinkedHashMap<>();
        fila.forEach((campo,valor)->salida.put(campo,valor instanceof BigDecimal n?n.toPlainString():valor));
        return salida;
    }

    private static BigDecimal importe(BigDecimal n) {
        if(n==null || n.signum()<0 || n.compareTo(MAXIMO)>0 || n.stripTrailingZeros().scale()>2) {
            throw invalido("Indica un importe válido con hasta dos decimales.");
        }
        return n.setScale(2);
    }

    private static String texto(String valor,int max,boolean obligatorio) {
        if(valor!=null) valor=valor.strip();
        if(valor==null || valor.isEmpty()) {
            if(obligatorio) throw invalido("Completa los campos obligatorios.");
            return null;
        }
        if(valor.length()>max || valor.chars().anyMatch(Character::isISOControl)) throw invalido("Texto inválido.");
        return valor;
    }

    private static int offset(int pagina) {
        if(pagina<0 || pagina>10000) throw invalido("Página inválida.");
        return pagina*50;
    }

    private static byte[] hash(String texto) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
        } catch(NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Identidad financiero() {
        var actor=identidad();
        if(actor.rol()!=Rol.ADMIN && actor.rol()!=Rol.RECEPCION) throw prohibido("El perfil no tiene acceso a Facturación.");
        return actor;
    }

    static LocalDateTime ahoraFactura() {
        return ahora();
    }
}
