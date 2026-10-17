package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.CitaParcialRq;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.CitaParcialService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
/** Valida la agenda; la misma regla se aplica al crear y al editar. */
@Service
public class CitaParcialServiceImpl implements CitaParcialService {
    private final CitaRepository citas;
    private final ClienteRepository clientes;
    private final MascotaRepository mascotas;
    private final MedicoRepository medicos;
    private static final Set<String> ESTADOS = Set.of("Pendiente", "Confirmada", "Atendida", "Cancelada", "Programada");
    public CitaParcialServiceImpl(CitaRepository citas, ClienteRepository clientes,
            MascotaRepository mascotas, MedicoRepository medicos) {
        this.citas = citas; this.clientes = clientes; this.mascotas = mascotas; this.medicos = medicos;
    }
    /** Lista por fecha y hora, de mayor a menor. */
    public List<Cita> listar() { return citas.findAllByOrderByFechaHoraDesc(); }
    /** Obtiene una cita o informa que no existe. */
    public Cita obtener(Long id) { return citas.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND,"No existe la cita.")); }
    /** Registra una cita con validaciones de pertenencia y horario. */
    public Cita guardar(CitaParcialRq datos) { return guardarDatos(new Cita(), datos, true); }
    /** No permite editar citas que ya fueron atendidas o canceladas. */
    public Cita actualizar(Long id, CitaParcialRq datos) {
        Cita actual = obtener(id);
        if (Set.of("Atendida", "Cancelada").contains(actual.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Una cita atendida o cancelada no se puede modificar.");
        return guardarDatos(actual,datos,false);
    }
    private synchronized Cita guardarDatos(Cita cita, CitaParcialRq d, boolean nueva) {
        if (d == null || d.clienteId() == null || d.mascotaId() == null || d.medicoId() == null ||
            d.fechaHora() == null || d.motivo() == null || d.motivo().isBlank() || !ESTADOS.contains(d.estado()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Completa cliente, mascota, veterinario, fecha, motivo y estado válido.");
        Cliente cliente = clientes.findById(d.clienteId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"No existe el cliente."));
        Mascota mascota = mascotas.findById(d.mascotaId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"No existe la mascota."));
        Medico medico = medicos.findById(d.medicoId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"No existe el veterinario."));
        if (mascota.getCliente() == null || !cliente.getClienteId().equals(mascota.getCliente().getClienteId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La mascota no pertenece al cliente.");
        if (d.fechaHora().isBefore(LocalDateTime.now()) &&
            (nueva || !d.fechaHora().equals(cita.getFechaHora())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No se permiten citas nuevas o reprogramadas en el pasado.");
        if (nueva && Set.of("Atendida", "Cancelada").contains(d.estado()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Una cita nueva debe comenzar activa.");
        if (!"Cancelada".equals(d.estado())) for (Cita otra : citas.findAll()) {
            if (cita.getId() != null && cita.getId().equals(otra.getId())) continue;
            if ("Cancelada".equals(otra.getEstado())) continue;
            long segundos = Math.abs(java.time.Duration.between(otra.getFechaHora(), d.fechaHora()).getSeconds());
            if (otra.getMedico().getId().equals(d.medicoId()) && segundos <= 15 * 60)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Veterinario ocupado en el intervalo de 15 minutos.");
            if (otra.getMascota().getMascotaId().equals(d.mascotaId()) && d.fechaHora().equals(otra.getFechaHora()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"La mascota ya tiene una cita en ese horario.");
        }
        cita.setCliente(cliente); cita.setMascota(mascota); cita.setMedico(medico);
        cita.setFechaHora(d.fechaHora()); cita.setMotivo(d.motivo().trim()); cita.setEstado(d.estado());
        return citas.save(cita);
    }
}
