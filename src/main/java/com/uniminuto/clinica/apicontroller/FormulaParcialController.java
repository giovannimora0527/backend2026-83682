package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaParcialRq;
import com.uniminuto.clinica.service.FormulaParcialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/** Endpoints para los tratamientos de la clínica. */
@RestController @RequestMapping("/api/formulas") @CrossOrigin(origins="http://localhost:4200")
public class FormulaParcialController {
    private final FormulaParcialService servicio;
    public FormulaParcialController(FormulaParcialService servicio) {this.servicio=servicio;}
    /** Lista fórmulas recientes primero. */
    @GetMapping public List<FormulaMedica> listar() {return servicio.listar();}
    /** Muestra el detalle de una fórmula. */
    @GetMapping("/{id}") public FormulaMedica obtener(@PathVariable Long id) {return servicio.obtener(id);}
    /** Crea fórmula asociada a consulta. */
    @PostMapping public ResponseEntity<FormulaMedica> guardar(@RequestBody FormulaParcialRq datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.guardar(datos));
    }
    /** Edita fórmula activa. */
    @PutMapping("/{id}") public FormulaMedica actualizar(@PathVariable Long id, @RequestBody FormulaParcialRq datos) {
        return servicio.actualizar(id,datos);
    }
}
