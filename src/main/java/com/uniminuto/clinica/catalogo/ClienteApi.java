package com.uniminuto.clinica.catalogo;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Propietarios de mascotas. */
@Tag(name = "Clientes", description = "Gestión de propietarios de mascotas")
@RequestMapping("/cliente")
public interface ClienteApi {

    /** Datos de un cliente (propietario de mascotas); la fecha de nacimiento en formato AAAA-MM-DD. */
    record ClienteRq(String tipoDocumento, String numeroDocumento, String nombres, String apellidos,
                     String fechaNacimiento, String genero, String telefono, String direccion) {
    }

    /**
     * Listar clientes (activos e inactivos).
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar clientes (activos e inactivos)")
    @PreAuthorize("hasAuthority('CLIENTE_CONSULTAR')")
    @GetMapping({"/listar-todos", "/listar"})
    ResponseEntity<List<ClienteDto>> listar();

    /**
     * Buscar cliente por número de documento.
     *
     * @param documento número de documento del cliente.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Buscar cliente por número de documento")
    @PreAuthorize("hasAuthority('CLIENTE_CONSULTAR')")
    @GetMapping("/buscar-por-documento")
    ResponseEntity<ClienteDto> buscarPorDocumento(@RequestParam String documento);

    /**
     * Consultar cliente.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Consultar cliente")
    @PreAuthorize("hasAuthority('CLIENTE_CONSULTAR')")
    @GetMapping("/{id}")
    ResponseEntity<ClienteDto> consultar(@PathVariable long id);

    /**
     * Listar las mascotas activas de un cliente (para combos dependientes cliente → mascota).
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar las mascotas activas de un cliente (para combos dependientes cliente → mascota)")
    @PreAuthorize("hasAuthority('CLIENTE_CONSULTAR')")
    @GetMapping("/{id}/mascotas")
    ResponseEntity<List<MascotaDeClienteDto>> mascotas(@PathVariable long id);

    /**
     * Registrar cliente.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar cliente")
    @PreAuthorize("hasAuthority('CLIENTE_GESTIONAR')")
    @PostMapping
    ResponseEntity<ClienteDto> crear(@RequestBody ClienteRq datos);

    /**
     * Actualizar cliente.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Actualizar cliente")
    @PreAuthorize("hasAuthority('CLIENTE_GESTIONAR')")
    @PutMapping("/{id}")
    ResponseEntity<ClienteDto> actualizar(@PathVariable long id, @RequestBody ClienteRq datos);

    /**
     * Archivar (inactivar) cliente.
     * <p>No elimina el registro; conserva su historial.</p>
     *
     * @param id identificador del registro.
     * @return respuesta sin contenido (204).
     */
    @Operation(summary = "Archivar (inactivar) cliente", description = "No elimina el registro; conserva su historial.")
    @PreAuthorize("hasAuthority('CLIENTE_ARCHIVAR')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> archivar(@PathVariable long id);

    /**
     * Reactivar un cliente archivado. Sus mascotas permanecen inactivas hasta que se activen una a una.
     *
     * @param id identificador del cliente.
     * @return respuesta HTTP con el cliente reactivado.
     */
    @Operation(summary = "Reactivar cliente archivado")
    @PreAuthorize("hasAuthority('CLIENTE_GESTIONAR')")
    @PatchMapping("/{id}/reactivar")
    ResponseEntity<ClienteDto> reactivar(@PathVariable long id);
}
