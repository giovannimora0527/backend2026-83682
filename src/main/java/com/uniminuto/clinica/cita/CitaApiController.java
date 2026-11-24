package com.uniminuto.clinica.cita;

import com.uniminuto.clinica.comun.Archivo;
import com.uniminuto.clinica.seguridad.SesionActual;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CitaApiController implements CitaApi {

    private final CitaService servicio;

    public CitaApiController(CitaService servicio) {
        this.servicio = servicio;
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }

    @Override
    public ResponseEntity<Map<String, Object>> crear(CitaRq datos) {
        return ResponseEntity.ok(servicio.crear(datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> actualizar(long id, CitaRq datos) {
        return ResponseEntity.ok(servicio.actualizar(id, datos));
    }

    @Override
    public ResponseEntity<Map<String, Object>> historialMascota(int id, Integer pagina) {
        return ResponseEntity.ok(servicio.historialMascota(id, pagina));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> agendaPropia(String desde, String hasta, String estado) {
        return ResponseEntity.ok(servicio.eventos(SesionActual.medicoId(), desde, hasta, estado));
    }

    @Override
    public ResponseEntity<byte[]> excelPropio(String desde, String hasta, String estado) {
        return Archivo.excel(servicio.excel(SesionActual.medicoId(), desde, hasta, estado),
                "citas-" + desde + "-" + hasta + ".xlsx");
    }

    @Override
    public ResponseEntity<Map<String, Object>> configuracionPropia() {
        return ResponseEntity.ok(servicio.configuracion(SesionActual.medicoId()));
    }

    @Override
    public ResponseEntity<Map<String, Object>> horariosPropios(List<Franja> franjas) {
        return ResponseEntity.ok(servicio.guardarHorarios(SesionActual.medicoId(), franjas));
    }

    @Override
    public ResponseEntity<Map<String, Object>> ausenciaPropia(AusenciaRq ausencia) {
        return ResponseEntity.ok(servicio.registrarAusencia(SesionActual.medicoId(), ausencia));
    }

    @Override
    public ResponseEntity<Map<String, Object>> disponibilidad(long medico, String fecha) {
        return ResponseEntity.ok(servicio.disponibilidad(medico, fecha));
    }

    @Override
    public ResponseEntity<Map<String, Object>> configuracion(long medico) {
        return ResponseEntity.ok(servicio.configuracion(medico));
    }

    @Override
    public ResponseEntity<Map<String, Object>> horarios(long medico, List<Franja> franjas) {
        return ResponseEntity.ok(servicio.guardarHorarios(medico, franjas));
    }

    @Override
    public ResponseEntity<Map<String, Object>> ausencia(long medico, AusenciaRq ausencia) {
        return ResponseEntity.ok(servicio.registrarAusencia(medico, ausencia));
    }

    @Override
    public ResponseEntity<Void> actividad(long medico, ActividadRq datos) {
        servicio.actividad(medico, datos == null ? null : datos.activo());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Map<String, Object>> cambiar(long id, CambioRq cambio) {
        return ResponseEntity.ok(servicio.cambiar(id, cambio));
    }

    @Override
    public ResponseEntity<List<Map<String, Object>>> historial(long id) {
        return ResponseEntity.ok(servicio.historial(id));
    }
}
