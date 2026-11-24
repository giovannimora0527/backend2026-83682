package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.EstadoRq;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.models.ServicioRq;
import com.uniminuto.clinica.models.ServicioRs;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RequestMapping("/servicio")
public interface ServicioApi {

    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<ServicioRs>> listar(@RequestParam(defaultValue = "false") boolean soloActivos, @RequestParam(required = false) Long especializacionId);

    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ServicioRs>> crear(@Valid @RequestBody ServicioRq rq);

    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ServicioRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody ServicioRq rq);

    @PatchMapping(value = "/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ServicioRs>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoRq rq);
}
