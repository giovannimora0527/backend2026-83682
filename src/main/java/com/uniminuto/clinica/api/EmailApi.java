package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.TestRs;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin(origins = "*")
@RequestMapping("/email")
public interface EmailApi {

    /**
     * Metodo test del servicio de envio de email.
     *
     * @return Servicio ok.
     * @throws BadRequestException excepcion.
     */
    @RequestMapping(value = "/test",
            produces = {"application/json"},
            consumes = {"application/json"},
            method = RequestMethod.GET)
    ResponseEntity<TestRs> testEmail()
            throws BadRequestException;
}
