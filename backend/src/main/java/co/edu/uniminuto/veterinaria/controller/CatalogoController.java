package co.edu.uniminuto.veterinaria.controller;

import co.edu.uniminuto.veterinaria.dto.MedicamentoResponse;
import co.edu.uniminuto.veterinaria.dto.VeterinarioResponse;
import co.edu.uniminuto.veterinaria.service.MedicamentoService;
import co.edu.uniminuto.veterinaria.service.VeterinarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST de catálogos usados por los combos: GET /api/veterinarios y GET /api/medicamentos.
 */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    /** Servicio de veterinarios. */
    private final VeterinarioService veterinarioService;

    /** Servicio de medicamentos. */
    private final MedicamentoService medicamentoService;

    /**
     * Crea el controlador.
     *
     * @param veterinarioService servicio de veterinarios
     * @param medicamentoService servicio de medicamentos
     */
    public CatalogoController(VeterinarioService veterinarioService, MedicamentoService medicamentoService) {
        this.veterinarioService = veterinarioService;
        this.medicamentoService = medicamentoService;
    }

    /**
     * Lista los veterinarios activos.
     *
     * @return veterinarios activos
     */
    @GetMapping("/veterinarios")
    public List<VeterinarioResponse> listarVeterinarios() {
        return veterinarioService.listar();
    }

    /**
     * Lista los medicamentos vigentes (no vencidos).
     *
     * @return medicamentos vigentes
     */
    @GetMapping("/medicamentos")
    public List<MedicamentoResponse> listarMedicamentos() {
        return medicamentoService.listarVigentes();
    }
}
