package com.uniminuto.clinica.catalogo;

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

import java.util.List;
import java.util.Map;

/** Mascotas (pacientes). */
@Tag(name = "Mascotas", description = "Gestión de mascotas o pacientes")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
@RequestMapping("/mascota")
public interface MascotaApi {

    record MascotaRq(String nombreMascota, Integer edad, Integer razaId, Long clienteId, Boolean activo) {
    }

    @Operation(summary = "Listar mascotas con raza y propietario")
    @GetMapping({"/listar-detalle", "/listar"})
    ResponseEntity<List<Map<String, Object>>> listar();

    @Operation(summary = "Consultar una mascota")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable int id);

    @Operation(summary = "Registrar mascota")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping("/crear")
    ResponseEntity<Map<String, Object>> crear(@RequestBody MascotaRq datos);

    @Operation(summary = "Actualizar mascota")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PutMapping("/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable int id, @RequestBody MascotaRq datos);
}
