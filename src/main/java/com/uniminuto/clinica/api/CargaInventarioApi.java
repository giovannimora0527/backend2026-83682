package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.CargaRs;
import com.uniminuto.clinica.models.RespuestaRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/inventario/carga")
public interface CargaInventarioApi {

    @GetMapping(value = "/plantilla")
    ResponseEntity<byte[]> plantilla(@RequestParam(defaultValue = "xlsx") String formato);

    @PostMapping(value = "/previsualizar", produces = {"application/json"}, consumes = {"multipart/form-data"})
    ResponseEntity<CargaRs> previsualizar(@RequestPart("archivo") MultipartFile archivo);

    @GetMapping(value = "/historial", produces = {"application/json"})
    ResponseEntity<List<CargaRs>> historial(@RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta);

    @GetMapping(value = "/{cargaId}", produces = {"application/json"})
    ResponseEntity<CargaRs> obtener(@PathVariable("cargaId") Long cargaId);

    @PostMapping(value = "/{cargaId}/confirmar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<CargaRs>> confirmar(@PathVariable("cargaId") Long cargaId);

    @PostMapping(value = "/{cargaId}/descartar", produces = {"application/json"})
    ResponseEntity<RespuestaRs<CargaRs>> descartar(@PathVariable("cargaId") Long cargaId);

    @GetMapping(value = "/{cargaId}/errores/archivo")
    ResponseEntity<byte[]> errores(@PathVariable("cargaId") Long cargaId,
            @RequestParam(defaultValue = "xlsx") String formato);
}
