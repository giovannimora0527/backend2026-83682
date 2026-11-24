package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/** Propietarios de mascotas. */
@Tag(name = "Clientes", description = "Gestión de propietarios de mascotas")
@Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION, Perfiles.MEDICO})
@RequestMapping("/cliente")
public interface ClienteApi {

    record ClienteRq(String tipoDocumento, String numeroDocumento, String nombres, String apellidos,
                     String fechaNacimiento, String genero, String telefono, String direccion) {
    }

    @Operation(summary = "Listar clientes (activos e inactivos)")
    @GetMapping({"/listar-todos", "/listar"})
    ResponseEntity<List<Map<String, Object>>> listar();

    @Operation(summary = "Buscar cliente por número de documento")
    @GetMapping("/buscar-por-documento")
    ResponseEntity<Map<String, Object>> buscarPorDocumento(@RequestParam String documento);

    @Operation(summary = "Consultar cliente")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable long id);

    @Operation(summary = "Registrar cliente")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping
    ResponseEntity<Map<String, Object>> crear(@RequestBody ClienteRq datos);

    @Operation(summary = "Actualizar cliente")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PutMapping("/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable long id, @RequestBody ClienteRq datos);

    @Operation(summary = "Archivar (inactivar) cliente", description = "No elimina el registro; conserva su historial.")
    @Permitido(Perfiles.ADMINISTRADOR)
    @DeleteMapping("/{id}")
    ResponseEntity<Void> archivar(@PathVariable long id);
}
