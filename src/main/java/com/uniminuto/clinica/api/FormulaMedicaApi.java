package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.coyote.BadRequestException;
import java.util.List;


// Aquí conserva el mismo import de BadRequestException
// que aparece en ClienteApi.java

/**
 * API que define los servicios web relacionados
 * con las fórmulas médicas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista todas las fórmulas médicas ordenadas
     * desde la más reciente hasta la más antigua.
     *
     * @return respuesta con la lista de fórmulas médicas.
     */
    @GetMapping(
            value = "/listar-todos",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;
}