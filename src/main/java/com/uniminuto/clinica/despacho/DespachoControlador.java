package com.uniminuto.clinica.despacho;

import static com.uniminuto.clinica.despacho.DespachoDatos.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.uniminuto.clinica.atencion.DocumentosClinicos;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/despacho")
class DespachoControlador {
    private final DespachoServicio servicio;
    private final DocumentosClinicos documentos;

    DespachoControlador(DespachoServicio servicio,DocumentosClinicos documentos) {
        this.servicio=servicio;
        this.documentos=documentos;
    }

    @GetMapping("/formulas")
    List<Formula> formulas(@RequestParam(required=false) Long id,@RequestParam(required=false) String propietario,
            @RequestParam(required=false) String mascota,@RequestParam(required=false) LocalDate fecha,
            @RequestParam(required=false) Estado estado,@RequestParam(defaultValue="0") int pagina) {
        return servicio.buscar(id,propietario,mascota,fecha,estado,pagina);
    }

    @GetMapping("/formulas/{id}")
    Detalle detalle(@PathVariable long id) { return servicio.consultar(id); }

    @PostMapping("/formulas/{id}/preparar")
    Plan preparar(@PathVariable long id,@RequestBody Preparar solicitud) { return servicio.preparar(id,solicitud); }

    @PostMapping("/formulas/{id}/disponibilidad")
    Detalle disponibilidad(@PathVariable long id,@RequestBody Motivo motivo) {
        return servicio.cambiar(id,Estado.PENDIENTE_DISPONIBILIDAD,motivo);
    }

    @PostMapping("/formulas/{id}/cancelar")
    Detalle cancelar(@PathVariable long id,@RequestBody Motivo motivo) {
        return servicio.cambiar(id,Estado.CANCELADO,motivo);
    }

    @GetMapping("/entregas")
    List<Entrega> historial(@RequestParam(required=false) LocalDate desde,@RequestParam(required=false) LocalDate hasta,
            @RequestParam(defaultValue="0") int pagina) {
        return servicio.historial(desde,hasta,pagina);
    }

    @GetMapping("/entregas/{id}")
    Entrega entrega(@PathVariable long id) { return servicio.consultarEntrega(id); }

    @PostMapping("/entregas/{id}/confirmar")
    Entrega confirmar(@PathVariable long id) { return servicio.confirmar(id); }

    @PostMapping("/entregas/{id}/revertir")
    Entrega revertir(@PathVariable long id,@RequestBody Motivo motivo) { return servicio.revertir(id,motivo); }

    @GetMapping("/entregas/{id}/pdf")
    ResponseEntity<byte[]> pdf(@PathVariable long id) throws IOException {
        var e=servicio.consultarEntrega(id);
        var f=servicio.origen(e);
        var fecha=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        var datos=new ArrayList<>(List.of("Fórmula n.º "+f.id()+" · Fecha (Bogotá): "+fecha.format(e.fecha()),
                "Veterinario: "+f.veterinario(), "Responsable: "+e.responsable(),
                "Propietario: "+f.propietario()+" · Mascota: "+f.mascota(),"Estado: "+e.estado().name().replace('_',' ')));
        for(var l:e.lineas()) datos.add(l.nombre()+" · Cantidad: "+l.cantidad()+" · Lote: "+l.numero());
        if(!e.pendiente().isEmpty()) {
            datos.add("Pendiente al confirmar la entrega:");
            datos.addAll(e.pendiente());
        }
        if(e.revertida()!=null) {
            datos.add("Entrega revertida por "+e.reversor()+" · "+fecha.format(e.revertida()));
            datos.add("Motivo: "+e.motivo());
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment()
                        .filename("despacho-"+id+".pdf").build().toString())
                .body(documentos.comprobante("Comprobante de despacho n.º "+id,datos));
    }
}
