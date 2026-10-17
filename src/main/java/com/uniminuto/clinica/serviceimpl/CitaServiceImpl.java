package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaParcialRq;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaParcialService;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/** Conserva los endpoints del trabajo anterior pero reutiliza las validaciones del parcial. */
@Service
public class CitaServiceImpl implements CitaService {
    private final CitaRepository citas;
    private final CitaParcialService parcial;
    public CitaServiceImpl(CitaRepository citas, CitaParcialService parcial) {
        this.citas = citas; this.parcial = parcial;
    }
    /** Filtra por rango de fechas como se solicitó en la primera actividad. */
    @Override public List<Cita> listarCita(LocalDateTime inicio, LocalDateTime fin) {
        return citas.findAllByFechaHoraBetweenOrderByFechaHoraDesc(inicio,fin);
    }
    /** Crea cita respetando las reglas del segundo parcial. */
    @Override public MiRespuestaRS guardarCita(CitaRq rq) {
        parcial.guardar(convertir(rq)); return respuesta("Cita guardada exitosamente");
    }
    /** Actualiza una cita activa; no permite saltarse las reglas por el endpoint antiguo. */
    @Override public MiRespuestaRS actualizarCita(CitaRq rq) {
        if (rq == null || rq.getCitaId() == null)
            throw new com.uniminuto.clinica.exception.BadRequestException("Indica el id de la cita.");
        parcial.actualizar(rq.getCitaId(), convertir(rq));
        return respuesta("Cita actualizada exitosamente");
    }
    private CitaParcialRq convertir(CitaRq r) {
        if (r == null) throw new com.uniminuto.clinica.exception.BadRequestException("La cita es obligatoria.");
        return new CitaParcialRq(r.getClienteId(),r.getMascotaId(),r.getMedicoId(),
            r.getFechaHora(),r.getMotivo(),r.getEstado());
    }
    private MiRespuestaRS respuesta(String msg) {
        MiRespuestaRS r = new MiRespuestaRS(); r.setStatus(200); r.setMessage(msg); return r;
    }
}
