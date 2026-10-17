package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.service.HistoriaParcialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/** API de historias y anotaciones de cada consulta. */
@RestController @RequestMapping("/api/historias") @CrossOrigin(origins="http://localhost:4200")
public class HistoriaParcialController {
    private final HistoriaParcialService servicio;
    public HistoriaParcialController(HistoriaParcialService servicio) {this.servicio=servicio;}
    /** Consulta historias recientes primero. */
    @GetMapping public List<HistoriaMedica> listar() {return servicio.listar();}
    /** Obtiene detalle clínico. */
    @GetMapping("/{id}") public HistoriaMedica obtener(@PathVariable Long id) {return servicio.obtener(id);}
    /** Registra una historia sobre una cita atendida. */
    @PostMapping public ResponseEntity<HistoriaMedica> guardar(@RequestBody HistoriaParcialRq datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(datos));
    }
    /** Anotaciones que pertenecen a una historia. */
    @GetMapping("/{id}/anotaciones") public List<AnotacionHistoria> anotaciones(@PathVariable Long id) {
        return servicio.anotaciones(id);
    }
    /** Agrega una anotación a una historia. */
    @PostMapping("/{id}/anotaciones") public ResponseEntity<AnotacionHistoria> agregar(@PathVariable Long id,
            @RequestBody AnotacionParcialRq datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.agregarAnotacion(id,datos));
    }
    /** Obtiene el detalle de una anotación. */
    @GetMapping("/anotaciones/{id}") public AnotacionHistoria detalle(@PathVariable Long id) {
        return servicio.obtenerAnotacion(id);
    }
}
