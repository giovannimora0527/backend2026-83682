package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta de las especializaciones existentes (tabla {@code especializacion}) para el formulario de médicos.
 */
@RestController
@RequestMapping("/especializaciones")
public class EspecializacionApiController {

    private final EspecializacionRepository especializaciones;

    public EspecializacionApiController(EspecializacionRepository especializaciones) {
        this.especializaciones = especializaciones;
    }

    /** Lista las especializaciones ordenadas por nombre. */
    @GetMapping(produces = "application/json")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Especializacion>> listar() {
        return ResponseEntity.ok(especializaciones.findAllByOrderByNombreAsc());
    }
}
