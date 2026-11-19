package com.uniminuto.clinica.facturacion;

import static com.uniminuto.clinica.facturacion.FacturaServicio.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import com.uniminuto.clinica.atencion.DocumentosClinicos;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facturacion")
class FacturaControlador {
    private final FacturaServicio servicio;
    private final DocumentosClinicos documentos;

    FacturaControlador(FacturaServicio servicio,DocumentosClinicos documentos) {
        this.servicio=servicio;
        this.documentos=documentos;
    }

    @GetMapping("/atenciones")
    Object elegibles(@RequestParam(defaultValue="0") int pagina) {
        return servicio.elegibles(pagina);
    }

    @PostMapping("/atenciones/{id}/previa")
    Object previa(@PathVariable long id,@RequestBody(required=false) Emitir solicitud) {
        return servicio.previa(id,solicitud==null?null:solicitud.descuento(),solicitud==null?null:solicitud.motivo());
    }

    @PostMapping("/atenciones/{id}/facturas")
    Detalle emitir(@PathVariable long id,@RequestBody Emitir solicitud) {
        return servicio.emitir(id,solicitud);
    }

    @GetMapping("/facturas")
    Object listar(@RequestParam(required=false) LocalDate desde,@RequestParam(required=false) LocalDate hasta,
            @RequestParam(required=false) String propietario,@RequestParam(required=false) String mascota,
            @RequestParam(required=false) Long propietarioId,@RequestParam(required=false) Long mascotaId,
            @RequestParam(required=false) String estado,@RequestParam(defaultValue="0") int pagina) {
        return servicio.listar(desde,hasta,propietario,mascota,propietarioId,mascotaId,estado,pagina);
    }

    @GetMapping("/facturas/{id}")
    Detalle consultar(@PathVariable long id) {
        return servicio.consultar(id);
    }

    @GetMapping("/facturas/{id}/pagos")
    Object pagos(@PathVariable long id) {
        return servicio.consultar(id).pagos();
    }

    @PostMapping("/facturas/{id}/pagos")
    Detalle registrar(@PathVariable long id,@RequestBody Pago solicitud) {
        return servicio.pago(id,solicitud);
    }

    @PostMapping("/facturas/{id}/anular")
    Detalle anular(@PathVariable long id,@RequestBody Anular solicitud) {
        return servicio.anular(id,solicitud);
    }

    @GetMapping("/facturas/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable long id) throws IOException {
        var d=servicio.consultar(id);
        var f=d.factura();
        var texto=new ArrayList<String>();
        texto.add("Documento interno del proyecto académico · Moneda COP");
        texto.add("Emisión (Bogotá): "+f.get("emitida")+" · Atención "+f.get("atencion")+" · Cita "+f.get("cita"));
        texto.add("Propietario: "+f.get("propietario")+" · Mascota: "+f.get("mascota"));
        texto.add("Veterinario: "+f.get("veterinario")+" · Especialidad: "+f.get("especialidad"));
        for(var l:d.lineas()) {
            String clase=l.get("servicio_id")==null?"Medicamento dispensado":"Servicio";
            texto.add(clase+" · "+l.get("concepto")+" · "+l.get("cantidad")+" × "+valor(l,"precio_unitario"));
            texto.add("Subtotal "+valor(l,"subtotal")+" · Descuento "+valor(l,"descuento")+" · IVA "+valor(l,"iva")
                    +" · Otros impuestos "+valor(l,"otros_impuestos")+" · Total "+valor(l,"total"));
        }
        texto.add("Subtotal: "+valor(f,"subtotal")+" · Descuento: "+valor(f,"descuento"));
        if(f.get("motivo_descuento")!=null) texto.add("Motivo del descuento: "+f.get("motivo_descuento"));
        texto.add("IVA: "+valor(f,"iva")+" · Otros impuestos: "+valor(f,"otros_impuestos"));
        texto.add("TOTAL: "+valor(f,"total")+" · Pagado: "+valor(f,"pagado")+" · Saldo: "+valor(f,"saldo"));
        texto.add("Estado: "+f.get("estado"));
        for(var p:d.pagos()) {
            texto.add("Pago "+p.get("id")+" · "+p.get("fecha")+" · "+valor(p,"importe")+" · "+p.get("metodo")
                    +(p.get("referencia")==null?"":" · Ref. "+p.get("referencia")));
        }
        for(var h:d.historial()) {
            if("ANULADA".equals(h.get("nuevo"))) {
                texto.add("Anulación: "+h.get("fecha")+" · "+h.get("responsable")+" · "+h.get("motivo"));
            }
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment()
                        .filename("factura-"+id+".pdf").build().toString())
                .body(documentos.comprobante("Factura "+f.get("numero"),texto));
    }

    private static String valor(Map<String,Object> fila,String campo) {
        return new BigDecimal(fila.get(campo).toString()).setScale(2).toPlainString();
    }
}
