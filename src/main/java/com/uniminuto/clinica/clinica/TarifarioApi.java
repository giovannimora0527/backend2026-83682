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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Requerimiento 3.1: módulo administrativo independiente de especialidades y tarifas.
 * Permite cambiar valores de consultas sin modificar el código fuente.
 */
@Tag(name = "Tarifario", description = "Especialidades, servicios, tarifas, IVA e impuestos (con historial)")
@Permitido(Perfiles.ADMINISTRADOR)
@RequestMapping("/tarifario")
public interface TarifarioApi {

    record EspecialidadRq(String codigo, String nombre, String descripcion, Boolean activo) {
    }

    record ServicioRq(String nombre) {
    }

    record TarifaRq(BigDecimal valor, BigDecimal iva, BigDecimal impuesto, String desde, String hasta) {
    }

    @Operation(summary = "Listar especialidades")
    @GetMapping("/especialidades")
    ResponseEntity<List<Map<String, Object>>> especialidades();

    @Operation(summary = "Registrar especialidad")
    @PostMapping("/especialidades")
    ResponseEntity<Map<String, Object>> crearEspecialidad(@RequestBody EspecialidadRq datos);

    @Operation(summary = "Editar, activar o inactivar especialidad")
    @PutMapping("/especialidades/{id}")
    ResponseEntity<Map<String, Object>> actualizarEspecialidad(@PathVariable long id, @RequestBody EspecialidadRq datos);

    @Operation(summary = "Servicios de una especialidad")
    @GetMapping("/especialidades/{id}/servicios")
    ResponseEntity<List<Map<String, Object>>> servicios(@PathVariable long id);

    @Operation(summary = "Agregar servicio o procedimiento a una especialidad")
    @PostMapping("/especialidades/{id}/servicios")
    ResponseEntity<Map<String, Object>> agregarServicio(@PathVariable long id, @RequestBody ServicioRq datos);

    @Operation(summary = "Historial de tarifas de un servicio (log de tarifas)")
    @GetMapping("/servicios/{id}/tarifas")
    ResponseEntity<List<Map<String, Object>>> tarifas(@PathVariable long id);

    @Operation(summary = "Registrar nueva vigencia de tarifa", description = "Cierra la vigencia anterior; no la borra.")
    @PostMapping("/servicios/{id}/tarifas")
    ResponseEntity<Map<String, Object>> nuevaTarifa(@PathVariable long id, @RequestBody TarifaRq datos);
}
