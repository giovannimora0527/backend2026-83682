package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.CitaHistorialEstado;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.CitaHistorialRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.EstadoCitaRq;
import com.uniminuto.clinica.models.EventoCalendarioRs;
import com.uniminuto.clinica.models.ReprogramarRq;
import com.uniminuto.clinica.repository.CitaHistorialEstadoRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.ServicioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.security.UsuarioAutenticado;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.RangoConsulta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final CitaHistorialEstadoRepository historialRepository;
    private final MascotaRepository mascotaRepository;
    private final MedicoRepository medicoRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendaService agenda;
    private final ReporteCitasExcel reporteExcel;
    private final UsuarioActual usuarioActual;

    public CitaServiceImpl(CitaRepository citaRepository, CitaHistorialEstadoRepository historialRepository,
                           MascotaRepository mascotaRepository, MedicoRepository medicoRepository,
                           ServicioRepository servicioRepository, UsuarioRepository usuarioRepository,
                           AgendaService agenda, ReporteCitasExcel reporteExcel, UsuarioActual usuarioActual) {
        this.citaRepository = citaRepository;
        this.historialRepository = historialRepository;
        this.mascotaRepository = mascotaRepository;
        this.medicoRepository = medicoRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
        this.agenda = agenda;
        this.reporteExcel = reporteExcel;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio no puede ser mayor a la fecha de fin");
        }
        return citaRepository.findByFechaInicioBetweenOrderByFechaInicio(fechaInicio, fechaFin);
    }

    @Override
    public List<Cita> listarCitasPorMascota(Integer mascotaId) {
        return citaRepository.findByMascotaOrderByFechaInicioDesc(mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new NotFoundException("La mascota no existe")));
    }

    @Override
    public Cita obtenerCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La cita no existe"));
        usuarioActual.verificarPropiedad(cita.getMedico().getId());
        return cita;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Cita crearCita(CitaRq rq) {
        validarDatos(rq);
        usuarioActual.verificarPropiedad(rq.getMedicoId());
        Servicio servicio = rq.getServicioId() == null ? null : servicioParaAgendar(rq.getServicioId());
        AgendaService.Espacio espacio = agenda.reservar(rq.getFechaHora(), rq.getMedicoId(), rq.getMascotaId(),
                rq.getClienteId(), servicio, true, null);
        Cita cita = new Cita();
        cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setUsuarioCreacionId(usuarioActual.obtener().id());
        cita.setServicio(servicio);
        cita.setMotivo(rq.getMotivo().trim());
        ocupar(cita, espacio);
        cita = citaRepository.saveAndFlush(cita);
        registrar(cita, null, null, null);
        return cita;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Cita actualizarCita(CitaRq rq) {
        validarDatos(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El id de la cita no puede ser nulo");
        }
        Cita cita = obtenerCita(rq.getId());
        usuarioActual.verificarPropiedad(rq.getMedicoId());
        EstadoCita anterior = cita.getEstado();
        EstadoCita nuevo = leerEstado(rq.getEstado(), anterior);
        if (nuevo == EstadoCita.CANCELADA && anterior != EstadoCita.CANCELADA) {
            return aplicarEstado(cita, nuevo, rq.getMotivoCancelacion());
        }
        exigirEditable(cita);
        if (nuevo != anterior) {
            exigirTransicion(anterior, nuevo);
        }
        Servicio servicio = cita.getServicio();
        if (rq.getServicioId() != null && (servicio == null || !servicio.getId().equals(rq.getServicioId()))) {
            servicio = servicioParaAgendar(rq.getServicioId());
        }
        LocalDateTime fechaAnterior = cita.getFechaInicio();
        boolean cambiaFecha = !fechaAnterior.equals(rq.getFechaHora().truncatedTo(ChronoUnit.MINUTES));
        AgendaService.Espacio espacio = agenda.reservar(rq.getFechaHora(), rq.getMedicoId(), rq.getMascotaId(),
                rq.getClienteId(), servicio, cambiaFecha, cita.getId());
        cita.setServicio(servicio);
        cita.setMotivo(rq.getMotivo().trim());
        cita.setEstado(nuevo);
        ocupar(cita, espacio);
        cita = citaRepository.saveAndFlush(cita);
        if (cambiaFecha || nuevo != anterior) {
            registrar(cita, anterior, cambiaFecha ? fechaAnterior : null, null);
        }
        return cita;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Cita reprogramar(Long id, ReprogramarRq rq) {
        Cita cita = obtenerCita(id);
        exigirEditable(cita);
        LocalDateTime anterior = cita.getFechaInicio();
        AgendaService.Espacio espacio = agenda.reservar(rq.fechaInicio(), cita.getMedico().getId(),
                cita.getMascota().getMascotaId(), null, cita.getServicio(), true, id);
        ocupar(cita, espacio);
        cita = citaRepository.saveAndFlush(cita);
        registrar(cita, cita.getEstado(), anterior, rq.motivo());
        return cita;
    }

    @Override
    @Transactional
    public Cita cambiarEstado(Long id, EstadoCitaRq rq) {
        return aplicarEstado(obtenerCita(id), rq.estado(), rq.motivo());
    }

    @Override
    public List<CitaHistorialRs> historialEstados(Long id) {
        obtenerCita(id);
        return historialRepository.findByCita_IdOrderByFechaAscIdAsc(id).stream().map(CitaHistorialRs::de).toList();
    }

    @Override
    public List<EventoCalendarioRs> calendario(String desde, String hasta, Long medicoId) {
        RangoConsulta rango = RangoConsulta.de(desde, hasta);
        return citaRepository.agenda(medicoEfectivo(medicoId), rango.inicio(), rango.fin()).stream()
                .map(EventoCalendarioRs::de).toList();
    }

    @Override
    public Map<EstadoCita, Long> resumenEstados(String desde, String hasta, Long medicoId) {
        RangoConsulta rango = RangoConsulta.de(desde, hasta);
        Map<EstadoCita, Long> resumen = new EnumMap<>(EstadoCita.class);
        for (EstadoCita estado : EstadoCita.values()) {
            resumen.put(estado, 0L);
        }
        citaRepository.contarPorEstado(medicoEfectivo(medicoId), rango.inicio(), rango.fin())
                .forEach(fila -> resumen.put((EstadoCita) fila[0], (Long) fila[1]));
        return resumen;
    }

    @Override
    public Descarga reporteExcel(String desde, String hasta, Long medicoId) {
        RangoConsulta rango = RangoConsulta.de(desde, hasta);
        Long id = medicoEfectivo(medicoId);
        Medico medico = id == null ? null : medicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El médico no existe."));
        // Por inicio de la cita, igual que el resumen por estado.
        List<Cita> citas = citaRepository.agenda(id, rango.inicio(), rango.fin()).stream()
                .filter(c -> !c.getFechaInicio().isBefore(rango.inicio())).toList();
        return reporteExcel.generar(citas, medico, rango, usuarioActual.obtener().username());
    }

    private Long medicoEfectivo(Long medicoId) {
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (!usuario.esMedico()) {
            return medicoId;
        }
        if (usuario.medicoId() == null) {
            throw new ConflictException("Su usuario no está vinculado a un médico.");
        }
        return usuario.medicoId();
    }

    private Cita aplicarEstado(Cita cita, EstadoCita nuevo, String motivo) {
        EstadoCita anterior = cita.getEstado();
        exigirTransicion(anterior, nuevo);
        String texto = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (nuevo == EstadoCita.CANCELADA) {
            if (texto == null) {
                throw new BadRequestException("El motivo de cancelación es obligatorio.");
            }
            if (texto.length() > 255) {
                throw new BadRequestException("El motivo admite hasta 255 caracteres.");
            }
            cita.setMotivoCancelacion(texto);
        }
        cita.setEstado(nuevo);
        Cita guardada = citaRepository.saveAndFlush(cita);
        registrar(guardada, anterior, null, texto);
        return guardada;
    }

    private static void exigirTransicion(EstadoCita anterior, EstadoCita nuevo) {
        if (!anterior.puedePasarA(nuevo)) {
            throw new ConflictException("estado", "La cita no puede pasar de " + anterior + " a " + nuevo + ".");
        }
    }

    private static void exigirEditable(Cita cita) {
        if (!cita.getEstado().esEditable()) {
            throw new ConflictException("Solo se editan o reprograman citas PROGRAMADAS o CONFIRMADAS; esta se encuentra "
                    + cita.getEstado() + ".");
        }
    }

    private Servicio servicioParaAgendar(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El servicio no existe."));
        Especializacion especializacion = servicio.getEspecializacion();
        if (!Boolean.TRUE.equals(servicio.getActivo())
                || especializacion != null && !Boolean.TRUE.equals(especializacion.getActivo())) {
            throw new ConflictException("servicioId", "El servicio o su especialidad están inactivos.");
        }
        return servicio;
    }

    private static void ocupar(Cita cita, AgendaService.Espacio espacio) {
        cita.setMedico(espacio.medico());
        cita.setMascota(espacio.mascota());
        cita.setCliente(espacio.mascota().getCliente());
        cita.setFechaInicio(espacio.inicio());
        cita.setFechaFin(espacio.fin());
    }

    private void registrar(Cita cita, EstadoCita anterior, LocalDateTime fechaAnterior, String motivo) {
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

    private static EstadoCita leerEstado(String valor, EstadoCita actual) {
        if (valor == null || valor.isBlank()) {
            return actual;
        }
        try {
            return EstadoCita.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El estado de la cita no es válido");
        }
    }

    private static void validarDatos(CitaRq rq) {
        if (rq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser nula");
        }
        if (rq.getMascotaId() == null) {
            throw new BadRequestException("El id de la mascota no puede ser nulo");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El id del medico no puede ser nulo");
        }
        if (rq.getMotivo() == null || rq.getMotivo().isBlank()) {
            throw new BadRequestException("El motivo de la cita no puede ser nulo o vacio");
        }
        if (rq.getMotivo().trim().length() > 255) {
            throw new BadRequestException("El motivo admite hasta 255 caracteres.");
        }
    }
}
