package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Servicios")
@RequestMapping("/servicio")
public interface ServicioApi {

    @Operation(summary = "Listar servicios con su tarifa vigente")
    @GetMapping(value = "/listar", produces = {"application/json"})
    ResponseEntity<List<ServicioRs>> listar(@RequestParam(defaultValue = "false") boolean soloActivos, @RequestParam(required = false) Long especializacionId);

    @Operation(summary = "Crear un servicio")
    @PostMapping(value = "/crear", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ServicioRs>> crear(@Valid @RequestBody ServicioRq rq);

    @Operation(summary = "Actualizar un servicio")
    @PutMapping(value = "/{id}", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ServicioRs>> actualizar(@PathVariable("id") Long id, @Valid @RequestBody ServicioRq rq);

    @Operation(summary = "Activar o inactivar un servicio")
    @PatchMapping(value = "/{id}/estado", produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<RespuestaRs<ServicioRs>> cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody EstadoRq rq);
}
