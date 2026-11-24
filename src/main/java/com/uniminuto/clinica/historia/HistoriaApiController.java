package com.uniminuto.clinica.historia;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HistoriaApiController implements HistoriaApi {

    private static final String FORMULA = """
            SELECT id, cita_id AS citaId, medicamento_id AS medicamentoId, dosis, frecuencia,
                   duracion AS duracionTratamiento, indicaciones, fecha_emision AS fechaEmision,
                   fecha_creacion_registro AS fechaCreacionRegistro,
                   fecha_actualizacion_registro AS fechaActualizacionRegistro
            FROM formula_medica
            """;

    private final Db db;

    public HistoriaApiController(Db db) {
        this.db = db;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> historias() {
        return ResponseEntity.ok(db.lista("""
                SELECT h.id, h.paciente_id AS pacienteId, h.fecha_creacion AS fechaCreacion, m.nombre_mascota AS mascota
                FROM historia_medica h JOIN mascota m ON m.mascota_id = h.paciente_id ORDER BY h.id DESC
                """));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> historiasPorPaciente(int pacienteId) {
        return ResponseEntity.ok(db.lista("""
                SELECT id, paciente_id AS pacienteId, fecha_creacion AS fechaCreacion
                FROM historia_medica WHERE paciente_id = :p
                """, Db.p("p", pacienteId)));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> anotaciones() {
        return ResponseEntity.ok(db.lista("""
                SELECT id, historia_id AS historiaId, medico_id AS medicoId, cita_id AS citaId, fecha, descripcion
                FROM anotacion_historia ORDER BY fecha DESC
                """));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> formulas() {
        return ResponseEntity.ok(db.lista(FORMULA + " ORDER BY fecha_creacion_registro DESC"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> formula(long id) {
        return ResponseEntity.ok(db.requerido(FORMULA + " WHERE id = :id", Db.p("id", id), "La fórmula no existe."));
    }

    @Override
    public ResponseEntity<Map<String, Object>> crearFormula(FormulaLegadaRq datos) {
        Map<String, Object> p = validar(datos);
        p.put("ahora", Tiempo.ahora());
        long id = db.insertar("""
                INSERT INTO formula_medica (cita_id, medicamento_id, dosis, frecuencia, duracion, indicaciones,
                                            fecha_emision, fecha_creacion_registro)
                VALUES (:cita, :med, :dosis, :frecuencia, :duracion, :indicaciones, :emision, :ahora)
                """, p);
        return formula(id);
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizarFormula(long id, FormulaLegadaRq datos) {
        formula(id);
        Map<String, Object> p = validar(datos);
        p.put("ahora", Tiempo.ahora());
        p.put("id", id);
        db.ejecutar("""
                UPDATE formula_medica SET cita_id = :cita, medicamento_id = :med, dosis = :dosis,
                       frecuencia = :frecuencia, duracion = :duracion, indicaciones = :indicaciones,
                       fecha_emision = :emision, fecha_actualizacion_registro = :ahora
                WHERE id = :id
                """, p);
        return formula(id);
    }

    private Map<String, Object> validar(FormulaLegadaRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la fórmula son obligatorios.");
        }
        Validar.requerido(d.citaId(), "cita");
        Validar.requerido(d.medicamentoId(), "medicamento");
        db.requerido("SELECT id FROM cita WHERE id = :id", Db.p("id", d.citaId()), "La cita no existe.");
        db.requerido("SELECT id FROM medicamento WHERE id = :id", Db.p("id", d.medicamentoId()),
                "El medicamento no existe.");
        return Db.p("cita", d.citaId(), "med", d.medicamentoId(), "dosis", Validar.texto(d.dosis(), "dosis", 255),
                "frecuencia", Validar.texto(d.frecuencia(), "frecuencia", 100),
                "duracion", Validar.texto(d.duracionTratamiento(), "duración", 100),
                "indicaciones", Validar.opcional(d.indicaciones(), "indicaciones", 2000),
                "emision", Validar.fecha(d.fechaEmision(), "fecha de emisión", false));
    }
}
