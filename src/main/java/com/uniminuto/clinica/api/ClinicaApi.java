package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.uniminuto.clinica.exception.BadRequestException;

@Tag(name = "Servicio")
@RequestMapping("/clinica")
public interface ClinicaApi {

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Comprobar que el servicio responde")
    @GetMapping(value = "/test",
            produces = {"application/text"})
    ResponseEntity<String> testService()
            throws BadRequestException;


    @Operation(summary = "Comprobación con sesión")
    @GetMapping(value = "/test2",
            produces = {"application/text"})
    ResponseEntity<String> testService2()
            throws BadRequestException;

    @Operation(summary = "Comprobación con sesión y respuesta JSON")
    @GetMapping(value = "/test3",
            produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> testService3()
            throws BadRequestException;
}
