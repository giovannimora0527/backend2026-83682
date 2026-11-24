package com.uniminuto.clinica.clinica;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Requerimiento 3.2 y 3.3: atención de la cita, fórmulas médicas y prefactura.
 */
@Tag(name = "Atención médica", description = "Atención de citas, fórmulas médicas y prefactura")
@RequestMapping("/atencion")
public interface AtencionApi {

    record ServicioRq(Long servicioId, Integer cantidad) {
    }

    record AtencionRq(Integer version, String motivo, String sintomas, String diagnostico, String observaciones,
                      String recomendaciones, List<ServicioRq> servicios) {
    }

    record LineaRq(Long medicamentoId, String dosis, String frecuencia, String duracion, Integer cantidad, String via,
                   String indicaciones) {
    }

    record FormulaRq(Integer revision, List<LineaRq> medicamentos) {
    }

    @Operation(summary = "Datos de la atención: mascota, antecedentes, servicios, catálogo con tarifas y fórmulas")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR})
    @GetMapping("/citas/{id}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable long id);

    @Operation(summary = "Abrir la atención de una cita asignada (crea la historia clínica si no existe)")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/citas/{id}/abrir")
    ResponseEntity<Map<String, Object>> abrir(@PathVariable long id);

    @Operation(summary = "Guardar motivo, síntomas, diagnóstico, observaciones, recomendaciones y servicios",
            description = "Requiere la versión actual (control de concurrencia optimista).")
    @Permitido(Perfiles.MEDICO)
    @PutMapping("/citas/{id}")
    ResponseEntity<Map<String, Object>> guardar(@PathVariable long id, @RequestBody AtencionRq datos);

    @Operation(summary = "Finalizar la atención (congela tarifas, marca la cita atendida y actualiza la historia)")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/citas/{id}/finalizar")
    ResponseEntity<Map<String, Object>> finalizar(@PathVariable long id, @RequestParam Integer version);

    @Operation(summary = "Prefactura PDF con servicios prestados y medicamentos formulados")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @GetMapping("/citas/{id}/prefactura")
    ResponseEntity<byte[]> prefactura(@PathVariable long id);

    @Operation(summary = "Fórmulas del médico autenticado")
    @Permitido(Perfiles.MEDICO)
    @GetMapping("/formulas")
    ResponseEntity<List<Map<String, Object>>> formulas();

    @Operation(summary = "Crear fórmula en borrador para la cita")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/citas/{id}/formulas")
    ResponseEntity<Map<String, Object>> crearFormula(@PathVariable long id, @RequestBody FormulaRq datos);

    @Operation(summary = "Editar fórmula en borrador")
    @Permitido(Perfiles.MEDICO)
    @PutMapping("/citas/{id}/formulas/{formula}")
    ResponseEntity<Map<String, Object>> actualizarFormula(@PathVariable long id, @PathVariable long formula,
                                                         @RequestBody FormulaRq datos);

    @Operation(summary = "Confirmar fórmula (valida existencias; queda pendiente de despacho, no descuenta inventario)")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/formulas/{id}/confirmar")
    ResponseEntity<Map<String, Object>> confirmar(@PathVariable long id, @RequestParam Integer revision);

    @Operation(summary = "Anular fórmula en borrador")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/formulas/{id}/anular")
    ResponseEntity<Map<String, Object>> anular(@PathVariable long id, @RequestParam Integer revision);

    @Operation(summary = "Imprimir fórmula médica confirmada (PDF)")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR, Perfiles.DESPACHO_MEDICO})
    @GetMapping("/formulas/{id}/pdf")
    ResponseEntity<byte[]> formulaPdf(@PathVariable long id);

    @Operation(summary = "Imprimir fórmula heredada de la versión anterior (PDF)")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @GetMapping("/formulas/legadas/{id}/pdf")
    ResponseEntity<byte[]> formulaLegadaPdf(@PathVariable long id);
}
