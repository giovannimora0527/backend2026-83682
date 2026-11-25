package com.uniminuto.clinica.clinica;

import com.uniminuto.clinica.comun.Db;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de {@link AtencionService}: concentra el SQL del módulo (consultas parametrizadas con nombre).
 * El servicio conserva las reglas de negocio y las transacciones.
 */
@Repository
public class AtencionRepository {

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public AtencionRepository(Db db) {
        this.db = db;
    }

    /**
     * Usada por {@link AtencionService} (consultar): SELECT s.servicio_id AS servicioId, sc.nombre, s.cantidad, s.valor_unitario AS valor, s.iva_porcentaje AS iva,…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAtencionServicio(Map<String, ?> p) {
        return db.lista("""
                    SELECT s.servicio_id AS servicioId, sc.nombre, s.cantidad, s.valor_unitario AS valor,
                           s.iva_porcentaje AS iva, s.impuesto_porcentaje AS impuesto
                    FROM atencion_servicio s JOIN servicio_clinico sc ON sc.id = s.servicio_id
                    WHERE s.atencion_id = :a ORDER BY s.id
                    """, p);
    }

    /**
     * Usada por {@link AtencionService} (consultar): SELECT an.fecha, CONCAT(md.nombres, ' ', md.apellidos) AS medico, an.descripcion FROM anotacion_historia an JO…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAnotacionHistoria(Map<String, ?> p) {
        return db.lista("""
                SELECT an.fecha, CONCAT(md.nombres, ' ', md.apellidos) AS medico, an.descripcion
                FROM anotacion_historia an
                JOIN historia_medica h ON h.id = an.historia_id
                JOIN medico md ON md.id = an.medico_id
                WHERE h.paciente_id = :m AND (an.cita_id IS NULL OR an.cita_id <> :c)
                ORDER BY an.fecha DESC LIMIT 30
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (consultar): SELECT id, especializacion_id AS especialidadId, nombre FROM servicio_clinico WHERE especializacion_id = :e AN…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarServicioClinico(Map<String, ?> p) {
        return db.lista("""
                SELECT id, especializacion_id AS especialidadId, nombre FROM servicio_clinico
                WHERE especializacion_id = :e AND activo = 1 ORDER BY nombre
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (abrir): SELECT id FROM historia_medica WHERE cita_id = :c
     *
     * @param p parámetros con nombre de la consulta.
     * @param tipo clase del valor esperado.
     * @param <T> tipo del valor.
     * @return resultado.
     */
    public <T> T valorHistoriaMedica(Map<String, ?> p, Class<T> tipo) {
        return db.valor("SELECT id FROM historia_medica WHERE cita_id = :c", p, tipo);
    }

    /**
     * Usada por {@link AtencionService} (abrir): INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, activo, fecha_creacion) VALUES (:…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarHistoriaMedica(Map<String, ?> p) {
        return db.insertar("""
                        INSERT INTO historia_medica (paciente_id, cita_id, medico_id, fecha, motivo, activo, fecha_creacion)
                        VALUES (:m, :c, :medico, :fecha, :motivo, 1, :ahora)
                        """, p);
    }

    /**
     * Usada por {@link AtencionService} (abrir): INSERT INTO atencion_medica (cita_id, historia_id, medico_id, especializacion_id, motivo, version, fecha_apert…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarAtencionMedica(Map<String, ?> p) {
        return db.insertar("""
                    INSERT INTO atencion_medica (cita_id, historia_id, medico_id, especializacion_id, motivo, version,
                                                 fecha_apertura)
                    VALUES (:c, :h, :m, :e, :motivo, 0, :ahora)
                    """, p);
    }

    /**
     * Usada por {@link AtencionService} (guardar): SELECT especializacion_id, activo, nombre FROM servicio_clinico WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerServicioClinico(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT especializacion_id, activo, nombre FROM servicio_clinico WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link AtencionService} (guardar): UPDATE atencion_medica SET motivo = :motivo, sintomas = :sintomas, diagnostico = :diagnostico, observaciones =…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarAtencionMedica(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE atencion_medica SET motivo = :motivo, sintomas = :sintomas, diagnostico = :diagnostico,
                       observaciones = :obs, recomendaciones = :rec, version = version + 1
                WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (guardar): DELETE FROM atencion_servicio WHERE atencion_id = :a
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int eliminarAtencionServicio(Map<String, ?> p) {
        return db.ejecutar("DELETE FROM atencion_servicio WHERE atencion_id = :a", p);
    }

    /**
     * Usada por {@link AtencionService} (finalizar): SELECT servicio_id, cantidad FROM atencion_servicio WHERE atencion_id = :a
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAtencionServicioEnFinalizar(Map<String, ?> p) {
        return db.lista("SELECT servicio_id, cantidad FROM atencion_servicio WHERE atencion_id = :a", p);
    }

    /**
     * Usada por {@link AtencionService} (finalizar): UPDATE atencion_medica SET fecha_finalizacion = :f, version = version + 1 WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarAtencionMedicaEnFinalizar(Map<String, ?> p) {
        return db.ejecutar("UPDATE atencion_medica SET fecha_finalizacion = :f, version = version + 1 WHERE id = :id", p);
    }

    /**
     * Usada por {@link AtencionService} (finalizar): UPDATE cita SET estado = 'atendida', fecha_modificacion = :f WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarCita(Map<String, ?> p) {
        return db.ejecutar("UPDATE cita SET estado = 'atendida', fecha_modificacion = :f WHERE id = :id", p);
    }

    /**
     * Usada por {@link AtencionService} (finalizar): INSERT INTO cita_revision (cita_id, usuario_id, fecha, estado_anterior, inicio_anterior, fin_anterior, estado_…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarCitaRevision(Map<String, ?> p) {
        return db.insertar("""
                    INSERT INTO cita_revision (cita_id, usuario_id, fecha, estado_anterior, inicio_anterior, fin_anterior,
                                               estado_nuevo, inicio_nuevo, fin_nuevo)
                    VALUES (:c, :u, :f, :ea, :i, :fin, 'atendida', :i, :fin)
                    """, p);
    }

    /**
     * Usada por {@link AtencionService} (finalizar): UPDATE historia_medica SET motivo = :motivo, diagnostico = :diagnostico, observaciones = :obs, fecha_modificac…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarHistoriaMedica(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE historia_medica SET motivo = :motivo, diagnostico = :diagnostico, observaciones = :obs,
                       fecha_modificacion = :f WHERE id = :h
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (finalizar): INSERT INTO anotacion_historia (historia_id, medico_id, cita_id, fecha, descripcion, observacion, diagnostico)…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarAnotacionHistoria(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO anotacion_historia (historia_id, medico_id, cita_id, fecha, descripcion, observacion, diagnostico)
                VALUES (:h, :m, :c, :f, :d, :o, :dx)
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (crearFormula): INSERT INTO formula_clinica (cita_id, atencion_id, medico_id, mascota_id, estado, revision, fecha_creacion) VA…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFormulaClinica(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO formula_clinica (cita_id, atencion_id, medico_id, mascota_id, estado, revision, fecha_creacion)
                VALUES (:c, :a, :m, :mascota, 'BORRADOR', 0, :f)
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (actualizarFormula): DELETE FROM formula_clinica_detalle WHERE formula_id = :f
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int eliminarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.ejecutar("DELETE FROM formula_clinica_detalle WHERE formula_id = :f", p);
    }

    /**
     * Usada por {@link AtencionService} (actualizarFormula): UPDATE formula_clinica SET revision = revision + 1 WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaClinica(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_clinica SET revision = revision + 1 WHERE id = :id", p);
    }

    /**
     * Usada por {@link AtencionService} (confirmarFormula): SELECT d.cantidad, m.nombre, COALESCE((SELECT SUM(l.cantidad) FROM medicamento_lote l WHERE l.medicamento_id =…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarMedicamentoLote(Map<String, ?> p) {
        return db.lista("""
                SELECT d.cantidad, m.nombre, COALESCE((SELECT SUM(l.cantidad) FROM medicamento_lote l
                       WHERE l.medicamento_id = m.id AND (l.fecha_vence IS NULL OR l.fecha_vence >= :hoy)), 0) AS disponible
                FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                WHERE d.formula_id = :f
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (confirmarFormula): UPDATE formula_clinica SET estado = 'CONFIRMADA', estado_despacho = 'PENDIENTE_DESPACHO', revision = revision …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaClinicaEnConfirmarFormula(Map<String, ?> p) {
        return db.ejecutar("""
                UPDATE formula_clinica SET estado = 'CONFIRMADA', estado_despacho = 'PENDIENTE_DESPACHO',
                       revision = revision + 1, fecha_confirmacion = :f WHERE id = :id
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (confirmarFormula): INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo) V…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarDespachoEstadoHistorial(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO despacho_estado_historial (formula_id, estado_anterior, estado_nuevo, usuario_id, fecha, motivo)
                VALUES (:f, 'BORRADOR', 'PENDIENTE_DESPACHO', :u, :ahora, 'Fórmula confirmada por el médico')
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (anularFormula): UPDATE formula_clinica SET estado = 'ANULADA', revision = revision + 1 WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas afectadas.
     */
    public int actualizarFormulaClinicaEnAnularFormula(Map<String, ?> p) {
        return db.ejecutar("UPDATE formula_clinica SET estado = 'ANULADA', revision = revision + 1 WHERE id = :id", p);
    }

    /**
     * Usada por {@link AtencionService} (formulas): SELECT d.medicamento_id AS medicamentoId, m.nombre, d.dosis, d.frecuencia, d.duracion, d.cantidad, d.via, d.in…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.lista("""
                    SELECT d.medicamento_id AS medicamentoId, m.nombre, d.dosis, d.frecuencia, d.duracion, d.cantidad,
                           d.via, d.indicaciones, m.precio_venta * d.cantidad AS precioEstimado
                    FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                    WHERE d.formula_id = :f ORDER BY d.id
                    """, p);
    }

    /**
     * Usada por {@link AtencionService} (validarLineas): SELECT nombre, tipo, activo FROM medicamento WHERE id = :id
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerMedicamento(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT nombre, tipo, activo FROM medicamento WHERE id = :id", p, mensaje);
    }

    /**
     * Usada por {@link AtencionService} (guardarLineas): INSERT INTO formula_clinica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion, cantidad, via, i…
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarFormulaClinicaDetalle(Map<String, ?> p) {
        return db.insertar("""
                    INSERT INTO formula_clinica_detalle (formula_id, medicamento_id, dosis, frecuencia, duracion, cantidad,
                                                         via, indicaciones)
                    VALUES (:f, :m, :dosis, :frecuencia, :duracion, :cantidad, :via, :ind)
                    """, p);
    }

    /**
     * Usada por {@link AtencionService} (formulaPdf): SELECT f.*, c.fecha_hora, m.nombre_mascota, r.especie, r.nombre AS raza, m.edad, CONCAT(cl.nombres, ' ', cl.ap…
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinica(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT f.*, c.fecha_hora, m.nombre_mascota, r.especie, r.nombre AS raza, m.edad,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, cl.tipo_documento, cl.numero_documento,
                       CONCAT(md.nombres, ' ', md.apellidos) AS medico, md.registro_profesional, e.nombre AS especialidad
                FROM formula_clinica f
                JOIN cita c ON c.id = f.cita_id
                JOIN mascota m ON m.mascota_id = f.mascota_id
                JOIN raza r ON r.raza_id = m.raza_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico md ON md.id = f.medico_id
                JOIN especializacion e ON e.id = md.especializacion_id
                WHERE f.id = :id
                """, p, mensaje);
    }

    /**
     * Usada por {@link AtencionService} (formulaPdf): SELECT m.nombre, m.presentacion, d.dosis, d.frecuencia, d.duracion, d.via, d.cantidad, d.indicaciones FROM for…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinicaDetalleEnFormulaPdf(Map<String, ?> p) {
        return db.lista("""
                SELECT m.nombre, m.presentacion, d.dosis, d.frecuencia, d.duracion, d.via, d.cantidad, d.indicaciones
                FROM formula_clinica_detalle d JOIN medicamento m ON m.id = d.medicamento_id WHERE d.formula_id = :f ORDER BY d.id
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (formulaLegadaPdf): SELECT fm.*, md.nombre AS medicamento, md.presentacion, c.fecha_hora, m.nombre_mascota, CONCAT(cl.nombres, ' '…
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaMedica(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT fm.*, md.nombre AS medicamento, md.presentacion, c.fecha_hora, m.nombre_mascota,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, CONCAT(me.nombres, ' ', me.apellidos) AS medico,
                       me.registro_profesional
                FROM formula_medica fm
                JOIN medicamento md ON md.id = fm.medicamento_id
                JOIN cita c ON c.id = fm.cita_id
                JOIN mascota m ON m.mascota_id = c.mascota_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico me ON me.id = c.medico_id
                WHERE fm.id = :id
                """, p, mensaje);
    }

    /**
     * Usada por {@link AtencionService} (prefactura): SELECT s.servicio_id, s.tarifa_id, sc.nombre, s.cantidad, s.valor_unitario, s.iva_porcentaje, s.impuesto_porce…
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarAtencionServicioEnPrefactura(Map<String, ?> p) {
        return db.lista("""
                SELECT s.servicio_id, s.tarifa_id, sc.nombre, s.cantidad, s.valor_unitario, s.iva_porcentaje, s.impuesto_porcentaje
                FROM atencion_servicio s JOIN servicio_clinico sc ON sc.id = s.servicio_id WHERE s.atencion_id = :a ORDER BY s.id
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (prefactura): SELECT md.id, md.nombre, SUM(d.cantidad) AS cantidad, md.precio_venta, md.iva_porcentaje FROM formula_clinica …
     *
     * @param p parámetros con nombre de la consulta.
     * @return filas.
     */
    public List<Map<String, Object>> listarFormulaClinica(Map<String, ?> p) {
        return db.lista("""
                SELECT md.id, md.nombre, SUM(d.cantidad) AS cantidad, md.precio_venta, md.iva_porcentaje
                FROM formula_clinica f JOIN formula_clinica_detalle d ON d.formula_id = f.id
                JOIN medicamento md ON md.id = d.medicamento_id
                WHERE f.cita_id = :c AND f.estado = 'CONFIRMADA'
                GROUP BY md.id, md.nombre, md.precio_venta, md.iva_porcentaje ORDER BY md.nombre
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (cita): SELECT c.*, m.nombre_mascota AS mascota, r.especie, r.nombre AS raza, m.edad, CONCAT(cl.nombres, ' ', cl.apell…
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCita(Map<String, ?> p, String mensaje) {
        return db.requerido("""
                SELECT c.*, m.nombre_mascota AS mascota, r.especie, r.nombre AS raza, m.edad,
                       CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario,
                       CONCAT(cl.tipo_documento, ' ', cl.numero_documento) AS documento, cl.telefono,
                       CONCAT(md.nombres, ' ', md.apellidos) AS medico, md.especializacion_id
                FROM cita c
                JOIN mascota m ON m.mascota_id = c.mascota_id
                JOIN raza r ON r.raza_id = m.raza_id
                JOIN cliente cl ON cl.cliente_id = c.cliente_id
                JOIN medico md ON md.id = c.medico_id
                WHERE c.id = :id
                """, p, mensaje);
    }

    /**
     * Usada por {@link AtencionService} (bloquearCita): SELECT id FROM cita WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerCitaEnBloquearCita(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT id FROM cita WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link AtencionService} (atencionDeCita): SELECT a.*, e.nombre AS especialidad FROM atencion_medica a JOIN especializacion e ON e.id = a.especializacion…
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarAtencionMedica(Map<String, ?> p) {
        return db.uno("""
                SELECT a.*, e.nombre AS especialidad FROM atencion_medica a
                JOIN especializacion e ON e.id = a.especializacion_id WHERE a.cita_id = :c
                """, p);
    }

    /**
     * Usada por {@link AtencionService} (bloquearAtencion): SELECT * FROM atencion_medica WHERE cita_id = :c FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @return fila.
     */
    public Map<String, Object> buscarAtencionMedicaEnBloquearAtencion(Map<String, ?> p) {
        return db.uno("SELECT * FROM atencion_medica WHERE cita_id = :c FOR UPDATE", p);
    }

    /**
     * Usada por {@link AtencionService} (bloquearFormula): SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE
     *
     * @param p parámetros con nombre de la consulta.
     * @param mensaje mensaje del 404 si no existe.
     * @return fila.
     */
    public Map<String, Object> obtenerFormulaClinicaEnBloquearFormula(Map<String, ?> p, String mensaje) {
        return db.requerido("SELECT * FROM formula_clinica WHERE id = :id FOR UPDATE", p, mensaje);
    }

    /**
     * Usada por {@link AtencionService} (insertarServicio): INSERT INTO atencion_servicio (atencion_id, servicio_id, tarifa_id, cantidad, valor_unitario, iva_porcentaje, …
     *
     * @param p parámetros con nombre de la consulta.
     * @return id generado.
     */
    public long insertarAtencionServicio(Map<String, ?> p) {
        return db.insertar("""
                INSERT INTO atencion_servicio (atencion_id, servicio_id, tarifa_id, cantidad, valor_unitario, iva_porcentaje,
                                               impuesto_porcentaje)
                VALUES (:a, :s, :t, :c, :v, :iva, :imp)
                """, p);
    }

    /** Criterios cerrados para consultar fórmulas. */
    public enum CriterioFormula {
        /** Fórmulas de un médico. */
        MEDICO("f.medico_id = :valor"),
        /** Una fórmula. */
        ID("f.id = :valor"),
        /** Fórmulas de una cita. */
        CITA("f.cita_id = :valor");

        /** Condición SQL fija del criterio. */
        private final String condicion;

        /**
         * @param condicion condición SQL.
         */
        CriterioFormula(String condicion) {
            this.condicion = condicion;
        }
    }

    /**
     * Fórmulas (encabezado) según un criterio, de la más reciente a la más antigua.
     *
     * @param criterio criterio.
     * @param valor valor buscado.
     * @return fórmulas.
     */
    public List<Map<String, Object>> formulasPor(CriterioFormula criterio, Object valor) {
        return db.lista("""
                SELECT f.id, f.cita_id AS citaId, m.nombre_mascota AS paciente, f.estado, f.estado_despacho AS estadoDespacho,
                       f.revision, f.fecha_creacion AS creada, f.fecha_confirmacion AS confirmada
                FROM formula_clinica f JOIN mascota m ON m.mascota_id = f.mascota_id
                WHERE """ + " " + criterio.condicion + " ORDER BY f.fecha_creacion DESC, f.id DESC", Db.p("valor", valor));
    }
}
