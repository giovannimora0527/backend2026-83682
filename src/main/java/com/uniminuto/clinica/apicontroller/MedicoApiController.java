package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Veterinario;
import com.uniminuto.clinica.service.VeterinarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Conserva las rutas del parcial bajo {@code /medico}. Usa la misma tabla {@code medico}
 * y el mismo servicio que {@link VeterinarioApiController} ({@code /veterinarios}).
 */
@RestController
@RequestMapping("/medico")
public class MedicoApiController {

    private final VeterinarioService service;

    public MedicoApiController(VeterinarioService service) {
        this.service = service;
    }

    /** Lista todos los médicos. */
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<List<Veterinario>> getMedicos() {
        return ResponseEntity.ok(service.listar());
    }

    /** Busca un médico por tipo y número de documento. */
    @GetMapping(value = "/buscar", produces = "application/json")
    public ResponseEntity<Veterinario> buscarMedico(@RequestParam String tipoDocumento,
                                                    @RequestParam String numeroDocumento) {
        return ResponseEntity.ok(service.buscarPorDocumento(tipoDocumento, numeroDocumento));
    }

    /** Busca un médico por su registro profesional. */
    @GetMapping(value = "/buscarPorRegistroProfesional", produces = "application/json")
    public ResponseEntity<Veterinario> buscarMedicoPorRegistroProfesional(@RequestParam String registroProfesional) {
        return ResponseEntity.ok(service.buscarPorRegistroProfesional(registroProfesional));
    }
}
