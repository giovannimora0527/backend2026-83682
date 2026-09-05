package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MascotaApi;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.service.MascotaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MascotaApiController implements MascotaApi {

    @Autowired
    private MascotaService mascotaService;

    @Override
    public ResponseEntity<List<Mascota>> listarMascotas() throws BadRequestException {
        return ResponseEntity.ok(this.mascotaService.listarMascotas());
    }

    @Override
    public ResponseEntity<List<Mascota>> listarMascotas(boolean ascendente) throws BadRequestException {
        return ResponseEntity.ok(this.mascotaService
                .listarMascotasOrdenado(ascendente));
    }

    @Override
    public ResponseEntity<List<Mascota>> buscarPorRaza(Integer razaId) throws BadRequestException {
        return ResponseEntity.ok(this.mascotaService
                .listarMascotasPorRaza(razaId));
    }

    @Override
    public ResponseEntity<List<Mascota>> buscarPorCliente(Long clienteId) throws BadRequestException {
        return ResponseEntity.ok(this.mascotaService
                .listarMascotasPorCliente(clienteId));
    }
}
