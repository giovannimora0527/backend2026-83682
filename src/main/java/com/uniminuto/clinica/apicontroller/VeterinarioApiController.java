
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Veterinario;
import com.uniminuto.clinica.service.VeterinarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST encargado de consultar los veterinarios registrados
 * en la clínica veterinaria.
 */
@RestController
@RequestMapping("/veterinarios")
public class VeterinarioApiController {

    /** Servicio que gestiona las operaciones de los veterinarios. */
    private final VeterinarioService service;

    /**
     * Construye el controlador con el servicio de veterinarios.
     *
     * @param service servicio encargado de gestionar los veterinarios
     */
    public VeterinarioApiController(VeterinarioService service) {
        this.service = service;
    }

    /**
     * Obtiene la lista de veterinarios registrados.
     *
     * @return respuesta HTTP con la lista de veterinarios
     */
    @GetMapping
    public ResponseEntity<List<Veterinario>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /**
     * Consulta un veterinario por su identificador.
     *
     * @param id identificador del veterinario
     * @return respuesta HTTP con los datos del veterinario consultado
     */
    @GetMapping("/{id}")
    public ResponseEntity<Veterinario> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(service.consultar(id));
    }
}
