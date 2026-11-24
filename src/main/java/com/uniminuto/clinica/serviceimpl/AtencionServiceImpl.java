package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.entity.AtencionServicio;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.FormulaMedicaDetalle;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.entity.enums.EstadoFormula;
import com.uniminuto.clinica.entity.enums.TipoServicio;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.exception.dto.ErrorResponse;
import com.uniminuto.clinica.models.AtencionCitaRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.AtencionRs;
import com.uniminuto.clinica.models.AtencionServicioRq;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.pdf.PrefacturaPdf;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.AtencionMedicaRepository;
import com.uniminuto.clinica.repository.AtencionServicioRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaDetalleRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.ServicioRepository;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.AtencionService;
import com.uniminuto.clinica.service.ConfiguracionService;
import com.uniminuto.clinica.service.FormulaService;
import com.uniminuto.clinica.util.RenglonCobro;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AtencionServiceImpl implements AtencionService {

    private final AtencionMedicaRepository atencionRepository;
    private final AtencionServicioRepository atencionServicioRepository;
    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final HistoriaMedicaRepository historiaRepository;
    private final AnotacionHistoriaRepository anotacionRepository;
    private final ServicioRepository servicioRepository;
    private final TarifaRepository tarifaRepository;
    private final FormulaMedicaRepository formulaRepository;
    private final FormulaMedicaDetalleRepository formulaDetalleRepository;
    private final FormulaService formulaService;
    private final HistorialCitaService historialCita;
    private final ConfiguracionService configuracion;
    private final UsuarioActual usuarioActual;
    private final int minutosAnticipacion;

    public AtencionServiceImpl(AtencionMedicaRepository atencionRepository,
                               AtencionServicioRepository atencionServicioRepository, CitaRepository citaRepository,
                               MascotaRepository mascotaRepository, HistoriaMedicaRepository historiaRepository,
                               AnotacionHistoriaRepository anotacionRepository, ServicioRepository servicioRepository,
                               TarifaRepository tarifaRepository, FormulaMedicaRepository formulaRepository,
                               FormulaMedicaDetalleRepository formulaDetalleRepository, FormulaService formulaService,
                               HistorialCitaService historialCita, ConfiguracionService configuracion,
                               UsuarioActual usuarioActual,
                               @Value("${atencion.minutos-anticipacion:30}") int minutosAnticipacion) {
        this.atencionRepository = atencionRepository;
        this.atencionServicioRepository = atencionServicioRepository;
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.historiaRepository = historiaRepository;
        this.anotacionRepository = anotacionRepository;
        this.servicioRepository = servicioRepository;
        this.tarifaRepository = tarifaRepository;
        this.formulaRepository = formulaRepository;
        this.formulaDetalleRepository = formulaDetalleRepository;
        this.formulaService = formulaService;
        this.historialCita = historialCita;
        this.configuracion = configuracion;
        this.usuarioActual = usuarioActual;
        this.minutosAnticipacion = minutosAnticipacion;
    }

    /** Idempotente: si la atencion ya existe, la devuelve. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AtencionRs iniciar(Long citaId) {
        Cita cita = citaRepository.bloquear(citaId).orElseThrow(() -> new NotFoundException("La cita no existe."));
        usuarioActual.verificarPropiedad(cita.getMedico().getId());
        Optional<AtencionMedica> existente = atencionRepository.findByCita_Id(citaId);
        if (existente.isPresent()) {
            return convertir(existente.get());
        }
        if (!cita.getEstado().esEditable() && cita.getEstado() != EstadoCita.EN_ATENCION) {
            throw new ConflictException("No se puede iniciar la atención de una cita " + cita.getEstado() + ".");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (!cita.getFechaInicio().toLocalDate().equals(ahora.toLocalDate())) {
            throw new ConflictException("La atención solo se inicia el día de la cita.");
        }
        if (ahora.isBefore(cita.getFechaInicio().minusMinutes(minutosAnticipacion))) {
            throw new ConflictException("La atención se puede iniciar desde " + minutosAnticipacion
                    + " minutos antes de la cita.");
        }
        Mascota mascota = mascotaRepository.bloquear(cita.getMascota().getMascotaId()).orElseThrow();
        AtencionMedica atencion = new AtencionMedica();
        atencion.setCita(cita);
        atencion.setHistoriaMedica(historiaRepository.findByMascota(mascota).orElseGet(() -> crearHistoria(mascota)));
        atencion.setMedico(cita.getMedico());
        atencion.setMascota(mascota);
        atencion.setMotivoConsulta(cita.getMotivo());
        atencion.setFechaInicio(ahora);
        atencion = atencionRepository.saveAndFlush(atencion);
        if (cita.getEstado() != EstadoCita.EN_ATENCION) {
            cambiarEstado(cita, EstadoCita.EN_ATENCION);
        }
        return convertir(atencion);
    }

    @Override
    @Transactional(readOnly = true)
    public AtencionCitaRs consultar(Long citaId) {
        Cita cita = citaRepository.findById(citaId).orElseThrow(() -> new NotFoundException("La cita no existe."));
        usuarioActual.verificarPropiedad(cita.getMedico().getId());
        Mascota mascota = cita.getMascota();
        Cliente cliente = cita.getCliente();
        Servicio servicio = cita.getServicio();
        Raza raza = mascota.getRaza();
        return new AtencionCitaRs(atencionRepository.findByCita_Id(citaId).map(this::convertir).orElse(null),
                new AtencionCitaRs.CitaResumen(citaId, cita.getFechaInicio(), cita.getFechaFin(), cita.getEstado(),
                        cita.getMotivo(), servicio == null ? null : servicio.getId(),
                        servicio == null ? null : servicio.getNombre()),
                new AtencionCitaRs.MascotaResumen(mascota.getMascotaId(), mascota.getNombreMascota(),
                        raza == null ? null : raza.getEspecie(), raza == null ? null : raza.getNombre(),
                        mascota.getEdad(), mascota.getActivo()),
                new AtencionCitaRs.Propietario(cliente.getId(), cliente.getNombres() + " " + cliente.getApellidos(),
                        cliente.getNumeroDocumento(), cliente.getTelefono(), cliente.getCorreo()),
                antecedentes(citaId, mascota.getMascotaId()));
    }

    @Override
    @Transactional
    public AtencionRs guardarBorrador(Long id, AtencionRq rq) {
        AtencionMedica atencion = bloquearAbierta(id);
        atencion.setMotivoConsulta(rq.motivoConsulta());
        atencion.setSintomas(rq.sintomas());
        atencion.setDiagnostico(rq.diagnostico());
        atencion.setObservaciones(rq.observaciones());
        atencion.setRecomendaciones(rq.recomendaciones());
        atencionRepository.saveAndFlush(atencion);
        if (rq.servicios() != null) {
            reemplazarServicios(atencion, rq.servicios());
        }
        return convertir(atencion);
    }

    @Override
    @Transactional
    public AtencionRs finalizar(Long id) {
        AtencionMedica atencion = bloquearAbierta(id);
        exigirDatos(atencion);
        Cita cita = atencion.getCita();
        if (formulaRepository.existsByCita_IdAndEstadoFormula(cita.getId(), EstadoFormula.BORRADOR)) {
            throw new ConflictException("Hay fórmulas en borrador; confírmelas o anúlelas antes de finalizar.");
        }
        List<AtencionServicio> servicios = atencionServicioRepository.findByAtencion_IdOrderById(id);
        if (servicios.isEmpty()) {
            servicios = List.of(agregar(atencion, servicioPorDefecto(cita), 1));
        }
        for (AtencionServicio s : servicios) {
            if (tarifaRepository.findByServicio_IdAndActivaTrue(s.getServicio().getId()).isEmpty()) {
                throw new ConflictException("servicios", "El servicio " + s.getServicio().getNombre()
                        + " no tiene tarifa vigente.");
            }
        }
        LocalDateTime ahora = LocalDateTime.now();
        atencion.setFechaFin(ahora);
        atencion.setFinalizada(true);
        atencionRepository.saveAndFlush(atencion);
        cambiarEstado(cita, EstadoCita.ATENDIDA);
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoria(atencion.getHistoriaMedica());
        anotacion.setMedico(atencion.getMedico());
        anotacion.setCita(cita);
        anotacion.setFecha(ahora);
        anotacion.setDescripcion("Motivo: " + atencion.getMotivoConsulta() + "\nSíntomas: " + atencion.getSintomas());
        anotacion.setDiagnostico(atencion.getDiagnostico());
        anotacion.setObservaciones(Stream.of(atencion.getObservaciones(),
                        vacio(atencion.getRecomendaciones()) ? null
                                : "Recomendaciones: " + atencion.getRecomendaciones())
                .filter(t -> !vacio(t)).collect(Collectors.joining("\n")));
        anotacionRepository.save(anotacion);
        return convertir(atencion);
    }

    @Override
    @Transactional(readOnly = true)
    public Descarga prefactura(Long id) {
        AtencionMedica atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La atención no existe."));
        usuarioActual.verificarPropiedad(atencion.getMedico().getId());
        List<RenglonCobro> renglones = new ArrayList<>();
        for (AtencionServicio s : atencionServicioRepository.findByAtencion_IdOrderById(id)) {
            Tarifa tarifa = tarifaRepository.findByServicio_IdAndActivaTrue(s.getServicio().getId()).orElse(null);
            renglones.add(RenglonCobro.de("Servicio", s.getServicio().getNombre()
                            + (tarifa == null ? " (sin tarifa vigente)" : ""), s.getCantidad(),
                    tarifa == null ? BigDecimal.ZERO : tarifa.getValor(), BigDecimal.ZERO,
                    tarifa == null ? BigDecimal.ZERO : tarifa.getImpuesto().getPorcentaje()));
        }
        List<Long> formulas = formulaRepository.findByCita_IdInOrderByFechaGeneracionDesc(
                        List.of(atencion.getCita().getId())).stream()
                .filter(f -> f.getEstadoFormula() == EstadoFormula.CONFIRMADA).map(FormulaMedica::getId).toList();
        for (FormulaMedicaDetalle d : formulaDetalleRepository.findByFormula_IdInOrderById(formulas)) {
            Medicamento m = d.getMedicamento();
            renglones.add(RenglonCobro.de("Medicamento", m.getNombre(), d.getCantidadPrescrita(), m.getPrecioVenta(),
                    BigDecimal.ZERO, m.getImpuesto() == null ? BigDecimal.ZERO : m.getImpuesto().getPorcentaje()));
        }
        return PrefacturaPdf.generar(configuracion.obtener(), atencion, renglones);
    }

    private AtencionCitaRs.Antecedentes antecedentes(Long citaId, Integer mascotaId) {
        List<AtencionMedica> previas = atencionRepository
                .findByMascota_MascotaIdAndFinalizadaTrueOrderByFechaInicioDesc(mascotaId).stream()
                .filter(a -> !a.getCita().getId().equals(citaId)).toList();
        Map<Long, List<AtencionServicio>> servicios = atencionServicioRepository
                .findByAtencion_IdInOrderById(previas.stream().map(AtencionMedica::getId).toList()).stream()
                .collect(Collectors.groupingBy(s -> s.getAtencion().getId()));
        Set<Long> citasAtendidas = previas.stream().map(a -> a.getCita().getId()).collect(Collectors.toSet());
        List<AtencionCitaRs.Anotacion> anotaciones = anotacionRepository
                .findByHistoria_Mascota_MascotaIdOrderByFechaDescIdDesc(mascotaId).stream()
                .filter(a -> a.getCita() == null || !citasAtendidas.contains(a.getCita().getId())
                        && !a.getCita().getId().equals(citaId))
                .map(a -> new AtencionCitaRs.Anotacion(a.getId(), a.getFecha(),
                        a.getMedico().getNombres() + " " + a.getMedico().getApellidos(), a.getDescripcion(),
                        a.getDiagnostico(), a.getObservaciones())).toList();
        return new AtencionCitaRs.Antecedentes(previas.stream()
                .map(a -> AtencionRs.de(a, servicios.getOrDefault(a.getId(), List.of()))).toList(), anotaciones,
                formulaService.antecedentes(mascotaId, citaId));
    }

    private void reemplazarServicios(AtencionMedica atencion, List<AtencionServicioRq> solicitados) {
        Set<Long> vistos = new HashSet<>();
        for (AtencionServicioRq s : solicitados) {
            if (!vistos.add(s.servicioId())) {
                throw new ConflictException("servicios", "Hay servicios repetidos; use la cantidad.");
            }
        }
        atencionServicioRepository.eliminarPorAtencion(atencion.getId());
        for (AtencionServicioRq s : solicitados) {
            Servicio servicio = servicioRepository.findById(s.servicioId())
                    .orElseThrow(() -> new NotFoundException("El servicio no existe."));
            if (!Boolean.TRUE.equals(servicio.getActivo())) {
                throw new ConflictException("servicios", "El servicio " + servicio.getNombre() + " está inactivo.");
            }
            agregar(atencion, servicio, s.cantidad() == null ? 1 : s.cantidad());
        }
    }

    private AtencionServicio agregar(AtencionMedica atencion, Servicio servicio, int cantidad) {
        AtencionServicio nuevo = new AtencionServicio();
        nuevo.setAtencion(atencion);
        nuevo.setServicio(servicio);
        nuevo.setCantidad(cantidad);
        return atencionServicioRepository.saveAndFlush(nuevo);
    }

    /** El servicio agendado; si no hay, la consulta de la especialidad del medico o la consulta general. */
    private Servicio servicioPorDefecto(Cita cita) {
        if (cita.getServicio() != null) {
            return cita.getServicio();
        }
        Long especialidad = cita.getMedico().getEspecializacion().getId();
        List<Servicio> consultas = servicioRepository.findByTipoServicioAndActivoTrueOrderById(TipoServicio.CONSULTA);
        return consultas.stream()
                .filter(s -> s.getEspecializacion() != null && s.getEspecializacion().getId().equals(especialidad))
                .findFirst()
                .or(() -> consultas.stream().filter(s -> s.getEspecializacion() == null).findFirst())
                .orElseThrow(() -> new ConflictException("servicios",
                        "Registre al menos un servicio: no hay una consulta por defecto para la especialidad."));
    }

    private static void exigirDatos(AtencionMedica a) {
        List<ErrorResponse.Detalle> faltantes = new ArrayList<>();
        if (vacio(a.getMotivoConsulta())) {
            faltantes.add(new ErrorResponse.Detalle("motivoConsulta", "El motivo de consulta es obligatorio."));
        }
        if (vacio(a.getSintomas())) {
            faltantes.add(new ErrorResponse.Detalle("sintomas", "Los síntomas son obligatorios."));
        }
        if (vacio(a.getDiagnostico())) {
            faltantes.add(new ErrorResponse.Detalle("diagnostico", "El diagnóstico es obligatorio."));
        }
        if (!faltantes.isEmpty()) {
            throw new BadRequestException("Complete los campos obligatorios antes de finalizar.", faltantes);
        }
    }

    private AtencionMedica bloquearAbierta(Long id) {
        AtencionMedica atencion = atencionRepository.bloquear(id)
                .orElseThrow(() -> new NotFoundException("La atención no existe."));
        usuarioActual.verificarPropiedad(atencion.getMedico().getId());
        if (Boolean.TRUE.equals(atencion.getFinalizada())) {
            throw new ConflictException("La atención ya fue finalizada.");
        }
        return atencion;
    }

    private void cambiarEstado(Cita cita, EstadoCita nuevo) {
        EstadoCita anterior = cita.getEstado();
        cita.setEstado(nuevo);
        citaRepository.saveAndFlush(cita);
        historialCita.registrar(cita, anterior, null, null);
    }

    private HistoriaMedica crearHistoria(Mascota mascota) {
        HistoriaMedica historia = new HistoriaMedica();
        historia.setMascota(mascota);
        historia.setFechaCreacion(LocalDateTime.now());
        return historiaRepository.save(historia);
    }

    private AtencionRs convertir(AtencionMedica atencion) {
        return AtencionRs.de(atencion, atencionServicioRepository.findByAtencion_IdOrderById(atencion.getId()));
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
