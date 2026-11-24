package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ClinicaApi;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.ClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClinicaApiController implements ClinicaApi {

    @Autowired
    private ClinicaService clinicaService;

    @Override
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> testService() throws BadRequestException {
        return ResponseEntity.ok("Servicio funcionando correctamente");
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> testService2() throws BadRequestException {
        return ResponseEntity.ok(clinicaService.testService2());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MiRespuestaRS> testService3()
            throws BadRequestException {
        return ResponseEntity.ok(clinicaService.testService3());
    }
}
