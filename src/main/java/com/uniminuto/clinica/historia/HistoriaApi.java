package com.uniminuto.clinica.historia;

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

import java.util.List;
import java.util.Map;

/**
 * Historias clínicas, anotaciones y fórmulas heredadas (tabla formula_medica de la entrega parcial).
 */
@Tag(name = "Historia clínica", description = "Historias, anotaciones y fórmulas heredadas")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO})
public interface HistoriaApi {

    record FormulaLegadaRq(Long citaId, Long medicamentoId, String dosis, String frecuencia,
                           String duracionTratamiento, String indicaciones, String fechaEmision) {
    }

    @Operation(summary = "Listar historias clínicas")
    @GetMapping("/historia-medica/listar-todos")
    ResponseEntity<List<Map<String, Object>>> historias();

    @Operation(summary = "Historias clínicas de una mascota")
    @GetMapping("/historia-medica/listar-por-paciente/{pacienteId}")
    ResponseEntity<List<Map<String, Object>>> historiasPorPaciente(@PathVariable int pacienteId);

    @Operation(summary = "Listar anotaciones de historias clínicas")
    @GetMapping("/anotacion-historia/listar-todos")
    ResponseEntity<List<Map<String, Object>>> anotaciones();

    @Operation(summary = "Listar fórmulas heredadas")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO, Perfiles.RECEPCION})
    @GetMapping({"/formula-medica/listar-todos", "/formula-medica/listar"})
    ResponseEntity<List<Map<String, Object>>> formulas();

    @Operation(summary = "Consultar fórmula heredada")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.MEDICO, Perfiles.RECEPCION})
    @GetMapping("/formula-medica/consultar/{id}")
    ResponseEntity<Map<String, Object>> formula(@PathVariable long id);

    @Operation(summary = "Registrar fórmula heredada (no afecta inventario)")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/formula-medica/crear")
    ResponseEntity<Map<String, Object>> crearFormula(@RequestBody FormulaLegadaRq datos);

    @Operation(summary = "Actualizar fórmula heredada")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PutMapping("/formula-medica/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizarFormula(@PathVariable long id, @RequestBody FormulaLegadaRq datos);
}
