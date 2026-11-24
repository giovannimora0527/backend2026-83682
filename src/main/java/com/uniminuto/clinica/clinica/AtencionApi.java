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

    /** Nombre de un servicio o procedimiento de la especialidad. */
    record ServicioRq(Long servicioId, Integer cantidad) {
    }

    /** Datos clínicos de la atención y servicios prestados; incluye la versión para el control de concurrencia. */
    record AtencionRq(Integer version, String motivo, String sintomas, String diagnostico, String observaciones,
                      String recomendaciones, List<ServicioRq> servicios) {
    }

    /** Línea de fórmula: medicamento, dosis, frecuencia, duración, cantidad, vía e indicaciones. */
    record LineaRq(Long medicamentoId, String dosis, String frecuencia, String duracion, Integer cantidad, String via,
                   String indicaciones) {
    }

    /** Fórmula clínica: revisión actual y lista de medicamentos prescritos. */
    record FormulaRq(Integer revision, List<LineaRq> medicamentos) {
    }

    /**
     * Datos de la atención: mascota, antecedentes, servicios, catálogo con tarifas y fórmulas.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Datos de la atención: mascota, antecedentes, servicios, catálogo con tarifas y fórmulas")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR})
    @GetMapping("/citas/{id}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable long id);

    /**
     * Abrir la atención de una cita asignada (crea la historia clínica si no existe).
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Abrir la atención de una cita asignada (crea la historia clínica si no existe)")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/citas/{id}/abrir")
    ResponseEntity<Map<String, Object>> abrir(@PathVariable long id);

    /**
     * Guardar motivo, síntomas, diagnóstico, observaciones, recomendaciones y servicios.
     * <p>Requiere la versión actual (control de concurrencia optimista).</p>
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Guardar motivo, síntomas, diagnóstico, observaciones, recomendaciones y servicios",
            description = "Requiere la versión actual (control de concurrencia optimista).")
    /**
     * Guarda el borrador de la atención (anamnesis, examen, diagnóstico y servicios).
     *
     * @param id identificador de la cita.
     * @param datos datos de la atención con su versión.
     * @return atención actualizada.
     */
    @Permitido(Perfiles.MEDICO)
    @PutMapping("/citas/{id}")
    ResponseEntity<Map<String, Object>> guardar(@PathVariable long id, @RequestBody AtencionRq datos);

    /**
     * Finalizar la atención (congela tarifas, marca la cita atendida y actualiza la historia).
     *
     * @param id identificador del registro.
     * @param version versión actual de la atención (control de concurrencia).
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Finalizar la atención (congela tarifas, marca la cita atendida y actualiza la historia)")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/citas/{id}/finalizar")
    ResponseEntity<Map<String, Object>> finalizar(@PathVariable long id, @RequestParam Integer version);

    /**
     * Prefactura PDF con servicios prestados y medicamentos formulados.
     *
     * @param id identificador del registro.
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Prefactura PDF con servicios prestados y medicamentos formulados")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @GetMapping("/citas/{id}/prefactura")
    ResponseEntity<byte[]> prefactura(@PathVariable long id);

    /**
     * Fórmulas del médico autenticado.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Fórmulas del médico autenticado")
    @Permitido(Perfiles.MEDICO)
    @GetMapping("/formulas")
    ResponseEntity<List<Map<String, Object>>> formulas();

    /**
     * Crear fórmula en borrador para la cita.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Crear fórmula en borrador para la cita")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/citas/{id}/formulas")
    ResponseEntity<Map<String, Object>> crearFormula(@PathVariable long id, @RequestBody FormulaRq datos);

    /**
     * Editar fórmula en borrador.
     *
     * @param id identificador del registro.
     * @param formula identificador de la fórmula.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Editar fórmula en borrador")
    @Permitido(Perfiles.MEDICO)
    @PutMapping("/citas/{id}/formulas/{formula}")
    ResponseEntity<Map<String, Object>> actualizarFormula(@PathVariable long id, @PathVariable long formula,
                                                         @RequestBody FormulaRq datos);

    /**
     * Confirmar fórmula (valida existencias; queda pendiente de despacho, no descuenta inventario).
     *
     * @param id identificador del registro.
     * @param revision revisión actual de la fórmula (control de concurrencia).
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Confirmar fórmula (valida existencias; queda pendiente de despacho, no descuenta inventario)")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/formulas/{id}/confirmar")
    ResponseEntity<Map<String, Object>> confirmar(@PathVariable long id, @RequestParam Integer revision);

    /**
     * Anular fórmula en borrador.
     *
     * @param id identificador del registro.
     * @param revision revisión actual de la fórmula (control de concurrencia).
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Anular fórmula en borrador")
    @Permitido(Perfiles.MEDICO)
    @PostMapping("/formulas/{id}/anular")
    ResponseEntity<Map<String, Object>> anular(@PathVariable long id, @RequestParam Integer revision);

    /**
     * Imprimir fórmula médica confirmada (PDF).
     *
     * @param id identificador del registro.
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Imprimir fórmula médica confirmada (PDF)")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR, Perfiles.DESPACHO_MEDICO})
    @GetMapping("/formulas/{id}/pdf")
    ResponseEntity<byte[]> formulaPdf(@PathVariable long id);

    /**
     * Imprimir fórmula heredada de la versión anterior (PDF).
     *
     * @param id identificador del registro.
     * @return archivo generado (PDF o Excel) para descargar.
     */
    @Operation(summary = "Imprimir fórmula heredada de la versión anterior (PDF)")
    @Permitido({Perfiles.MEDICO, Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @GetMapping("/formulas/legadas/{id}/pdf")
    ResponseEntity<byte[]> formulaLegadaPdf(@PathVariable long id);
}
