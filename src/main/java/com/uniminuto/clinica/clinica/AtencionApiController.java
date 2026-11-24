package com.uniminuto.clinica.clinica;

import com.uniminuto.clinica.comun.Archivo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AtencionApiController implements AtencionApi {

    private final AtencionService servicio;

    public AtencionApiController(AtencionService servicio) {
        this.servicio = servicio;
    }

    @Override
    public ResponseEntity<Map<String, Object>> consultar(long id) {
        return ResponseEntity.ok(servicio.consultar(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> abrir(long id) {
        return ResponseEntity.ok(servicio.abrir(id));
    }

    @Override
    public ResponseEntity<Map<String, Object>> guardar(long id, AtencionRq datos) {
        return ResponseEntity.ok(servicio.guardar(id, datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> finalizar(long id, Integer version) {
        return ResponseEntity.ok(servicio.finalizar(id, version));
    }

    @Override
    public ResponseEntity<byte[]> prefactura(long id) {
        return Archivo.pdf(servicio.prefactura(id), "prefactura-" + id + ".pdf");
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> formulas() {
        return ResponseEntity.ok(servicio.formulasPropias());
    }

    @Override
    public ResponseEntity<Map<String, Object>> crearFormula(long id, FormulaRq datos) {
        return ResponseEntity.ok(servicio.crearFormula(id, datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizarFormula(long id, long formula, FormulaRq datos) {
        return ResponseEntity.ok(servicio.actualizarFormula(id, formula, datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> confirmar(long id, Integer revision) {
        return ResponseEntity.ok(servicio.confirmarFormula(id, revision));
    }

    @Override
    public ResponseEntity<Map<String, Object>> anular(long id, Integer revision) {
        return ResponseEntity.ok(servicio.anularFormula(id, revision));
    }

    @Override
    public ResponseEntity<byte[]> formulaPdf(long id) {
        return Archivo.pdf(servicio.formulaPdf(id), "formula-" + id + ".pdf");
    }

    @Override
    public ResponseEntity<byte[]> formulaLegadaPdf(long id) {
        return Archivo.pdf(servicio.formulaLegadaPdf(id), "formula-historica-" + id + ".pdf");
    }
}
