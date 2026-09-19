package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.PacienteApi;
import com.uniminuto.clinica.dto.PacienteDTO;
import com.uniminuto.clinica.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de Paciente.
 *
 * <p>{@code @RestController} le indica a Spring que esta clase expone
 * servicios web y que las respuestas de sus metodos se deben convertir
 * automaticamente a JSON.</p>
 *
 * <p>{@code @CrossOrigin} permite que otras aplicaciones (por ejemplo, un
 * frontend corriendo en otro puerto o dominio) puedan consumir esta API sin
 * ser bloqueadas por la politica de seguridad del navegador (CORS).</p>
 *
 * <p>{@code @RequestMapping("/api/pacientes")} define el prefijo comun de
 * ruta para todos los metodos de este controlador.</p>
 */
@RestController
@CrossOrigin
@RequestMapping("/api/pacientes")
public class PacienteController implements PacienteApi {

    /** Servicio con la logica de negocio de Paciente. */
    private final PacienteService pacienteService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param pacienteService servicio de pacientes
     */
    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code POST /api/pacientes}.
     * {@code @Valid} hace que Spring valide automaticamente el DTO segun
     * las anotaciones definidas en {@link PacienteDTO} (por ejemplo
     * {@code @NotBlank}) antes de ejecutar el metodo.</p>
     */
    @Override
    @PostMapping
    public ResponseEntity<PacienteDTO> crearPaciente(@Valid @RequestBody PacienteDTO pacienteDTO) {
        PacienteDTO creado = pacienteService.crearPaciente(pacienteDTO);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Responde a peticiones {@code GET /api/pacientes}.</p>
     */
    @Override
    @GetMapping
    public ResponseEntity<List<PacienteDTO>> listarPacientes() {
        return ResponseEntity.ok(pacienteService.listarPacientes());
    }
}
