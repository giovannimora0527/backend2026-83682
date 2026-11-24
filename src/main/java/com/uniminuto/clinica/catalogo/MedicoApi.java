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

@Tag(name = "Médicos", description = "Médicos veterinarios")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
@RequestMapping("/medico")
public interface MedicoApi {

    record MedicoRq(String tipoDocumento, String numeroDocumento, String nombres, String apellidos, String telefono,
                    String registroProfesional, Long especializacionId, Long usuarioId, Boolean activo) {
    }

    @Operation(summary = "Listar médicos con su especialidad")
    @GetMapping("/listar")
    ResponseEntity<List<Map<String, Object>>> listar();

    @Operation(summary = "Listar médicos activos (asignables a citas)")
    @GetMapping("/listar-disponibles")
    ResponseEntity<List<Map<String, Object>>> disponibles();

    @Operation(summary = "Registrar médico")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PostMapping("/crear")
    ResponseEntity<Map<String, Object>> crear(@RequestBody MedicoRq datos);

    @Operation(summary = "Actualizar médico")
    @Permitido(Perfiles.ADMINISTRADOR)
    @PutMapping("/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable long id, @RequestBody MedicoRq datos);
}
