package co.edu.uniminuto.veterinaria.controller;

import co.edu.uniminuto.veterinaria.dto.MascotaResponse;
import co.edu.uniminuto.veterinaria.service.MascotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST de mascotas: GET /api/mascotas/{id}.
 */
@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    /** Servicio de mascotas. */
    private final MascotaService mascotaService;

    /**
     * Crea el controlador.
     *
     * @param mascotaService servicio de mascotas
     */
    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    /**
     * Consulta una mascota.
     *
     * @param id identificador de la mascota
     * @return la mascota
     */
    @GetMapping("/{id}")
    public MascotaResponse obtener(@PathVariable Long id) {
        return mascotaService.obtener(id);
    }
}
