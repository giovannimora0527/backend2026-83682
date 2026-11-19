package com.uniminuto.clinica.atencion;

import static com.uniminuto.clinica.atencion.ClinicaDatos.*;

import com.uniminuto.clinica.service.FormulaMedicaService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClinicaControlador {
    private final TarifasServicio tarifas;
    private final AtencionServicio atencion;
    private final DocumentosClinicos documentos;
    private final FormulaMedicaService legadas;
    private final ClinicaRepositorio datos;

    public ClinicaControlador(TarifasServicio tarifas,AtencionServicio atencion,DocumentosClinicos documentos,
                              FormulaMedicaService legadas,ClinicaRepositorio datos) {
        this.tarifas=tarifas;this.atencion=atencion;this.documentos=documentos;this.legadas=legadas;this.datos=datos;
    }

    @GetMapping("/tarifario/especialidades")
    List<Especialidad> especialidades() { return tarifas.especialidades(); }
    @PostMapping("/tarifario/especialidades")
    @ResponseStatus(HttpStatus.CREATED)
    Especialidad crear(@RequestBody Especialidad input) { return tarifas.guardar(null,input); }
    @PutMapping("/tarifario/especialidades/{id}")
    Especialidad editar(@PathVariable long id,@RequestBody Especialidad input) { return tarifas.guardar(id,input); }
    @GetMapping("/tarifario/especialidades/{id}/servicios")
    List<Servicio> servicios(@PathVariable long id) { return tarifas.servicios(id); }
    @PostMapping("/tarifario/especialidades/{id}/servicios")
    @ResponseStatus(HttpStatus.CREATED)
    Servicio servicio(@PathVariable long id,@RequestBody Map<String,String> input) {
        return tarifas.agregarServicio(id,input.get("nombre"));
    }
    @GetMapping("/tarifario/servicios/{id}/tarifas")
    List<Tarifa> historial(@PathVariable long id) { return tarifas.historial(id); }
    @PostMapping("/tarifario/servicios/{id}/tarifas")
    @ResponseStatus(HttpStatus.CREATED)
    Tarifa tarifa(@PathVariable long id,@RequestBody Tarifa input) { return tarifas.agregarTarifa(id,input); }

    @GetMapping("/atencion/citas/{id}")
    Atencion consulta(@PathVariable long id) { return atencion.consultar(id); }
    @PostMapping("/atencion/citas/{id}/abrir")
    Atencion abrir(@PathVariable long id) { return atencion.abrir(id); }
    @PutMapping("/atencion/citas/{id}")
    Atencion guardar(@PathVariable long id,@RequestBody GuardarAtencion input) { return atencion.guardar(id,input); }
    @PostMapping("/atencion/citas/{id}/finalizar")
    Atencion finalizar(@PathVariable long id,@RequestParam int version) { return atencion.finalizar(id,version); }
    @GetMapping("/atencion/citas/{id}/prefactura")
    ResponseEntity<byte[]> prefactura(@PathVariable long id) throws IOException {
        return pdf("prefactura-"+id,documentos.prefactura(atencion.consultar(id)));
    }
    @GetMapping("/atencion/formulas")
    List<Formula> formulas() { return atencion.formulas(); }
    @PostMapping("/atencion/citas/{id}/formulas")
    @ResponseStatus(HttpStatus.CREATED)
    Formula formula(@PathVariable long id,@RequestBody EdicionFormula input) {
        return atencion.guardarFormula(id,null,input);
    }
    @PutMapping("/atencion/citas/{id}/formulas/{formula}")
    Formula actualizarFormula(@PathVariable long id,@PathVariable long formula,@RequestBody EdicionFormula input) {
        return atencion.guardarFormula(id,formula,input);
    }
    @PostMapping("/atencion/formulas/{id}/confirmar")
    Formula confirmar(@PathVariable long id,@RequestParam int revision) { return atencion.resolverFormula(id,revision,true); }
    @PostMapping("/atencion/formulas/{id}/anular")
    Formula anular(@PathVariable long id,@RequestParam int revision) { return atencion.resolverFormula(id,revision,false); }
    @GetMapping("/atencion/formulas/{id}/pdf")
    ResponseEntity<byte[]> formulaPdf(@PathVariable long id) throws IOException {
        Formula f=atencion.formula(id);
        return pdf("formula-"+id,documentos.formula(f,atencion.consultar(f.citaId()).cita()));
    }
    @GetMapping("/atencion/formulas/legadas/{id}/pdf")
    ResponseEntity<byte[]> legada(@PathVariable long id) throws IOException {
        var f=legadas.obtenerFormulaPorId(id);
        var p=atencion.consultar(f.getCitaId()).cita();
        var medicamento=datos.uno("SELECT nombre FROM medicamento WHERE id=?",(r,i)->r.getString(1),"Medicamento",f.getMedicamentoId());
        return pdf("formula-historica-"+id,documentos.legada(f,p,medicamento));
    }

    private ResponseEntity<byte[]> pdf(String nombre,byte[] contenido) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(nombre+".pdf").build().toString())
                .cacheControl(CacheControl.noStore()).body(contenido);
    }
}
