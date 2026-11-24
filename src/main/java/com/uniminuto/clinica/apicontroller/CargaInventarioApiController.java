package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CargaInventarioApi;
import com.uniminuto.clinica.models.CargaRs;
import com.uniminuto.clinica.models.RespuestaRs;
import com.uniminuto.clinica.service.CargaInventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@RestController
public class CargaInventarioApiController implements CargaInventarioApi {

    private final CargaInventarioService servicio;

    public CargaInventarioApiController(CargaInventarioService servicio) {
        this.servicio = servicio;
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> plantilla(String formato) {
        return servicio.plantilla(formato).respuesta();
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CargaRs> previsualizar(MultipartFile archivo) {
        return ResponseEntity.ok(servicio.previsualizar(archivo));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CargaRs>> historial(LocalDate desde,
            LocalDate hasta) {
        return ResponseEntity.ok(servicio.historial(desde, hasta));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CargaRs> obtener(Long cargaId) {
        return ResponseEntity.ok(servicio.obtener(cargaId));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<CargaRs>> confirmar(Long cargaId) {
        return ResponseEntity.ok(RespuestaRs.ok("Carga confirmada.",
                servicio.confirmar(cargaId)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<RespuestaRs<CargaRs>> descartar(Long cargaId) {
        return ResponseEntity.ok(RespuestaRs.ok("Carga descartada.",
                servicio.descartar(cargaId)));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<byte[]> errores(Long cargaId,
            String formato) {
        return servicio.errores(cargaId, formato).respuesta();
    }
}
