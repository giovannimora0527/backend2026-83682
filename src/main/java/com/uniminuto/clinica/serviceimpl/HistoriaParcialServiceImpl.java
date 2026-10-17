package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.HistoriaParcialService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
/** Mantiene cada historia asociada a la consulta correcta. */
@Service
public class HistoriaParcialServiceImpl implements HistoriaParcialService {
    private final HistoriaMedicaRepository historias;
    private final CitaRepository citas;
    private final MascotaRepository mascotas;
    private final MedicoRepository medicos;
    private final AnotacionHistoriaRepository notas;
    public HistoriaParcialServiceImpl(HistoriaMedicaRepository historias, CitaRepository citas,
            MascotaRepository mascotas, MedicoRepository medicos, AnotacionHistoriaRepository notas) {
        this.historias=historias; this.citas=citas; this.mascotas=mascotas; this.medicos=medicos; this.notas=notas;
    }
    /** Historias recientes primero. */
    public List<HistoriaMedica> listar() { return historias.findAllByOrderByFechaCreacionDesc(); }
    /** Devuelve una historia por identificador. */
    public HistoriaMedica obtener(Long id) { return historias.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND,"Historia clínica no encontrada.")); }
    /** Una cita atendida puede generar una historia sin duplicar su consulta. */
    public HistoriaMedica guardar(HistoriaParcialRq d) {
        if (d == null || d.mascotaId() == null || d.citaId() == null || d.medicoId() == null ||
            d.diagnostico() == null || d.diagnostico().isBlank() || d.motivoConsulta() == null || d.motivoConsulta().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Indica mascota, cita, veterinario, motivo y diagnóstico.");
        Mascota mascota = mascotas.findById(d.mascotaId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Mascota no encontrada."));
        Cita cita = citas.findById(d.citaId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Cita no encontrada."));
        Medico medico = medicos.findById(d.medicoId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Veterinario no encontrado."));
        if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId()) ||
            !cita.getMedico().getId().equals(medico.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La mascota y el veterinario deben coincidir con la cita.");
        if (!"Atendida".equals(cita.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"La cita debe estar Atendida antes de generar historia.");
        if (historias.existsByCitaId(cita.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Esta cita ya tiene historia clínica.");
        HistoriaMedica h = new HistoriaMedica(); h.setPacienteId(mascota.getMascotaId());
        h.setCita(cita); h.setMedico(medico); h.setFechaCreacion(cita.getFechaHora());
        h.setMotivoConsulta(d.motivoConsulta()); h.setDiagnostico(d.diagnostico());
        h.setObservaciones(d.observaciones()); h.setEstado("Atendida");
        return historias.save(h);
    }
    /** Lista las notas de una historia, ordenadas de más reciente a más antigua. */
    public List<AnotacionHistoria> anotaciones(Long historiaId) {
        obtener(historiaId); return notas.findByHistoriaIdOrderByFechaDesc(historiaId);
    }
    /** Agrega una nota a una historia existente. */
    public AnotacionHistoria agregarAnotacion(Long historiaId, AnotacionParcialRq d) {
        obtener(historiaId);
        if (d == null || d.medicoId() == null || d.descripcion() == null || d.descripcion().isBlank() ||
            d.evolucion() == null || d.evolucion().isBlank() ||
            d.observacion() == null || d.observacion().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Completa veterinario, descripción, observación y evolución.");
        medicos.findById(d.medicoId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Veterinario no encontrado."));
        AnotacionHistoria nota = new AnotacionHistoria(); nota.setHistoriaId(historiaId);
        nota.setMedicoId(d.medicoId()); nota.setFecha(LocalDateTime.now());
        nota.setDescripcion(d.descripcion()); nota.setObservacion(d.observacion()); nota.setEvolucion(d.evolucion());
        return notas.save(nota);
    }
    /** Obtiene el texto completo de una anotación. */
    public AnotacionHistoria obtenerAnotacion(Long id) { return notas.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND,"Anotación no encontrada.")); }
}
