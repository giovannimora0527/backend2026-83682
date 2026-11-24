package com.uniminuto.clinica.cita;

import com.uniminuto.clinica.comun.Archivo;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del módulo: implementa {@link CitaApi} (rutas, permisos y documentación Swagger)
 * y delega la lógica en la capa de servicio.
 */
@RestController
public class CitaApiController implements CitaApi {

    /** Dependencia inyectada por constructor: servicio. */
    private final CitaService servicio;

    /**
     * Crea el controlador con sus dependencias (inyección por constructor).
     *
     * @param servicio dependencia servicio.
     */
    public CitaApiController(CitaService servicio) {
        this.servicio = servicio;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> listarRango(String fechaInicio, String fechaFin) {
        return ResponseEntity.ok(servicio.listarRango(fechaInicio, fechaFin));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> porMascota(int mascotaId) {
        return ResponseEntity.ok(servicio.porMascota(mascotaId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> consultar(long id) {
        return ResponseEntity.ok(servicio.detalle(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> reactivar(long id, CambioRq horario) {
        return ResponseEntity.ok(servicio.reactivar(id, horario));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> crear(CitaRq datos) {
        return ResponseEntity.ok(servicio.crear(datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> actualizar(long id, CitaRq datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> historialMascota(int id, Integer pagina) {
        return ResponseEntity.ok(servicio.historialMascota(id, pagina));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> agendaPropia(String desde, String hasta, String estado) {
        return ResponseEntity.ok(servicio.eventos(SesionActual.medicoId(), desde, hasta, estado));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> excelPropio(String desde, String hasta, String estado) {
        return Archivo.excel(servicio.excel(SesionActual.medicoId(), desde, hasta, estado),
                "citas-" + desde + "-" + hasta + ".xlsx");
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> configuracionPropia() {
        return ResponseEntity.ok(servicio.configuracion(SesionActual.medicoId()));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> horariosPropios(List<Franja> franjas) {
        return ResponseEntity.ok(servicio.guardarHorarios(SesionActual.medicoId(), franjas));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> ausenciaPropia(AusenciaRq ausencia) {
        return ResponseEntity.ok(servicio.registrarAusencia(SesionActual.medicoId(), ausencia));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> eliminarAusenciaPropia(long id) {
        return ResponseEntity.ok(servicio.eliminarAusencia(SesionActual.medicoId(), id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> ausenciasPropias(String desde, String hasta) {
        return ResponseEntity.ok(servicio.ausenciasEnRango(SesionActual.medicoId(), desde, hasta));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> disponibilidad(long medico, String fecha) {
        return ResponseEntity.ok(servicio.disponibilidad(medico, fecha));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> configuracion(long medico) {
        return ResponseEntity.ok(servicio.configuracion(medico));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> horarios(long medico, List<Franja> franjas) {
        return ResponseEntity.ok(servicio.guardarHorarios(medico, franjas));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> ausencia(long medico, AusenciaRq ausencia) {
        return ResponseEntity.ok(servicio.registrarAusencia(medico, ausencia));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> eliminarAusencia(long medico, long id) {
        return ResponseEntity.ok(servicio.eliminarAusencia(medico, id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> actividad(long medico, ActividadRq datos) {
        servicio.actividad(medico, datos == null ? null : datos.activo());
        return ResponseEntity.noContent().build();
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> cambiar(long id, CambioRq cambio) {
        return ResponseEntity.ok(servicio.cambiar(id, cambio));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Map<String, Object>>> historial(long id) {
        return ResponseEntity.ok(servicio.historial(id));
    }
}
