package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EmailApi;
import com.uniminuto.clinica.models.TestRs;
import com.uniminuto.clinica.service.EmailService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST API para email.
 *
 * @author lmora
 */
@RestController
public class EmailApiController implements EmailApi {
    /**
     * Servicio de envio de correos.
     */
    @Autowired
    private EmailService emailService;

    @Override
    public ResponseEntity<TestRs> testEmail() throws BadRequestException {
      return ResponseEntity.ok(emailService.testService());
    }
}