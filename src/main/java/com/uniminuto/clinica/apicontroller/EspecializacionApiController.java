package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EspecializacionApiController implements EspecializacionApi {

    @Autowired
    private EspecializacionService especializacionService;

    @Override
    public ResponseEntity<List<Especializacion>> listarEspecializaciones() throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.listarEspecializaciones());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearEspecializacion(Especializacion especializacion) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.crearEspecializacion(especializacion));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarEspecializacion(Especializacion especializacion) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.actualizarEspecializacion(especializacion));
    }
}
