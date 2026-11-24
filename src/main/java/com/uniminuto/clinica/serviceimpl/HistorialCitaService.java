package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.CitaHistorialEstado;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.repository.CitaHistorialEstadoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/** Registra cada cambio de estado o de fecha de una cita. */
@Component
public class HistorialCitaService {

    private final CitaHistorialEstadoRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;

    public HistorialCitaService(CitaHistorialEstadoRepository historialRepository,
                                UsuarioRepository usuarioRepository, UsuarioActual usuarioActual) {
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioActual = usuarioActual;
    }

    /**
     * @param anterior      nulo al crear
     * @param fechaAnterior nula si la fecha no cambio
     */
    public void registrar(Cita cita, EstadoCita anterior, LocalDateTime fechaAnterior, String motivo) {
        CitaHistorialEstado registro = new CitaHistorialEstado();
        registro.setCita(cita);
        registro.setEstadoAnterior(anterior);
        registro.setEstadoNuevo(cita.getEstado());
        registro.setFechaInicioAnterior(fechaAnterior);
        registro.setFechaInicioNueva(anterior == null || fechaAnterior != null ? cita.getFechaInicio() : null);
        registro.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
        registro.setUsuario(usuarioRepository.getReferenceById(usuarioActual.obtener().id()));
        registro.setFecha(LocalDateTime.now());
        historialRepository.save(registro);
    }
}
