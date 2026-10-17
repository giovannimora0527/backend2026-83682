package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaParcialRq;
import com.uniminuto.clinica.service.CitaParcialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/** Endpoints académicos para administrar la agenda. */
@RestController @RequestMapping("/api/citas") @CrossOrigin(origins="http://localhost:4200")
public class CitaParcialController {
    private final CitaParcialService servicio;
    public CitaParcialController(CitaParcialService servicio) {this.servicio=servicio;}
    /** Consulta todas las citas ordenadas. */
    @GetMapping public List<Cita> listar() {return servicio.listar();}
    /** Consulta una cita específica. */
    @GetMapping("/{id}") public Cita obtener(@PathVariable Long id) {return servicio.obtener(id);}
    /** Guarda una cita nueva. */
    @PostMapping public ResponseEntity<Cita> guardar(@RequestBody CitaParcialRq datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(datos));
    }
    /** Actualiza una cita activa. */
    @PutMapping("/{id}") public Cita actualizar(@PathVariable Long id, @RequestBody CitaParcialRq datos) {
        return servicio.actualizar(id,datos);
    }
}
