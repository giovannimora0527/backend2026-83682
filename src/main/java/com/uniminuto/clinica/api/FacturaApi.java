package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.FacturaRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/factura")
public interface FacturaApi {

    @GetMapping(value = "/cliente/{clienteId}", produces = {"application/json"})
    ResponseEntity<List<FacturaRs>> listarPorCliente(@PathVariable("clienteId") Long clienteId);

    @GetMapping(value = "/mascota/{mascotaId}", produces = {"application/json"})
    ResponseEntity<List<FacturaRs>> listarPorMascota(@PathVariable("mascotaId") Integer mascotaId);
}
