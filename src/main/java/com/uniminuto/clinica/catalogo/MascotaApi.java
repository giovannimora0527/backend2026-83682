package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.seguridad.Perfiles;
import com.uniminuto.clinica.seguridad.Permitido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    /** Datos de una mascota: nombre, edad, raza, propietario y estado. */
    record MascotaRq(String nombreMascota, Integer edad, Integer razaId, Long clienteId, Boolean activo) {
    }

    /**
     * Listar mascotas con raza y propietario.
     *
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Listar mascotas con raza y propietario")
    @GetMapping({"/listar-detalle", "/listar"})
    ResponseEntity<List<Map<String, Object>>> listar();

    /**
     * Consultar una mascota.
     *
     * @param id identificador del registro.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Consultar una mascota")
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> consultar(@PathVariable int id);

    /**
     * Registrar mascota.
     *
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Registrar mascota")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PostMapping("/crear")
    ResponseEntity<Map<String, Object>> crear(@RequestBody MascotaRq datos);

    /**
     * Actualizar mascota.
     *
     * @param id identificador del registro.
     * @param datos datos enviados en el cuerpo de la solicitud.
     * @return respuesta HTTP con el resultado en formato JSON.
     */
    @Operation(summary = "Actualizar mascota")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PutMapping("/actualizar/{id}")
    ResponseEntity<Map<String, Object>> actualizar(@PathVariable int id, @RequestBody MascotaRq datos);

    /** Nuevo estado de la mascota. */
    record EstadoRq(Boolean activo) {
    }

    /**
     * Activar o inactivar una mascota. No se puede inactivar con citas futuras reservadas
     * ni activar si su propietario está archivado.
     *
     * @param id identificador de la mascota.
     * @param datos nuevo estado.
     * @return respuesta HTTP con la mascota actualizada.
     */
    @Operation(summary = "Activar o inactivar mascota")
    @Permitido({Perfiles.ADMINISTRADOR, Perfiles.RECEPCION})
    @PatchMapping("/{id}/estado")
    ResponseEntity<Map<String, Object>> cambiarEstado(@PathVariable int id, @RequestBody EstadoRq datos);
}
