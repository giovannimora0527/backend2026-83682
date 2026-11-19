package com.uniminuto.clinica.serviceimpl;

import static com.uniminuto.clinica.calendario.AgendaServicio.*;

import com.uniminuto.clinica.calendario.AgendaRepositorio;
import com.uniminuto.clinica.calendario.AgendaDatos.Cambio;
import com.uniminuto.clinica.calendario.AgendaServicio;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.AlcanceMedico;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CitaServiceImpl implements CitaService {
    private final CitaRepository citas;
    private final AgendaRepositorio datos;
    private final AgendaServicio agenda;
    private final AlcanceMedico alcance;

    public CitaServiceImpl(CitaRepository citas, AgendaRepositorio datos, AgendaServicio agenda, AlcanceMedico alcance) {
        this.citas=citas;
        this.datos=datos;
        this.agenda=agenda;
        this.alcance=alcance;
    }

    @Override
    @Transactional(readOnly=true)
    public List<Cita> buscarCitasPorFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde==null || hasta==null || hasta.isBefore(desde)) throw invalido("El rango de fechas no es válido.");
        Integer medico=alcance.medicoId();
        return medico==null ? citas.findByFechaHoraBetweenOrderByFechaHoraDesc(desde,hasta)
                : citas.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraDesc(medico,desde,hasta);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Cita> listarCitas() {
        Integer medico=alcance.medicoId();
        return medico==null ? citas.findAllByOrderByFechaHoraDesc() : citas.findByMedicoIdOrderByFechaHoraDesc(medico);
    }

    @Override
    @Transactional(readOnly=true)
    public Cita consultarCitaPorId(Long id) {
        Cita cita=citas.findById(id).orElseThrow(()->noExiste("Cita"));
        alcance.exigirMedico(cita.getMedicoId());
        return cita;
    }

    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Cita crearCita(Cita cita) {
        validarCampos(cita);
        String estado=estado(cita.getEstado());
        if (!reserva(estado)) throw invalido("Una cita nueva debe iniciar como pendiente o confirmada.");
        LocalDateTime fin=cita.getFechaFin();
        var periodo=intervalo(cita.getFechaHora(),fin);
        validarFutura(periodo.inicio());
        bloquearRecursos(cita,cita,true);
        agenda.validarReserva(cita.getMedicoId(),cita.getMascotaId(),null,periodo);
        cita.setId(null);
        cita.setEstado(estado);
        cita.setFechaFin(fin);
        Cita guardada=citas.saveAndFlush(cita);
        registrar(guardada,null);
        return guardada;
    }

    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Cita actualizarCita(Long id,Cita cambios) {
        validarCampos(cambios);
        return actualizar(citas.bloquearPorId(id).orElseThrow(()->noExiste("Cita")),cambios);
    }

    @Override
    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Cita cambiarAgenda(Long id,Cambio cambio) {
        if (cambio==null) throw invalido("Debes enviar los cambios de la cita.");
        Cita actual=citas.bloquearPorId(id).orElseThrow(()->noExiste("Cita"));
        alcance.exigirMedico(actual.getMedicoId());
        Cita cambios=new Cita();
        cambios.setMedicoId(actual.getMedicoId());
        cambios.setMascotaId(actual.getMascotaId());
        cambios.setClienteId(actual.getClienteId());
        cambios.setMotivo(actual.getMotivo());
        cambios.setFechaHora(cambio.inicio()==null ? actual.getFechaHora() : cambio.inicio());
        cambios.setFechaFin(cambio.fin());
        cambios.setEstado(cambio.estado()==null ? actual.getEstado() : cambio.estado());
        return actualizar(actual,cambios);
    }

    private Cita actualizar(Cita actual,Cita cambios) {
        alcance.exigirEdicionCita(actual,cambios);
        String anterior=estado(actual.getEstado());
        String siguiente=estado(cambios.getEstado());
        validarTransicion(anterior,siguiente);
        LocalDateTime fin=cambios.fechaFinRegistrada();
        if (fin==null) fin=cambios.getFechaHora().plus(Duration.between(actual.getFechaHora(),actual.getFechaFin()));
        var periodo=intervalo(cambios.getFechaHora(),fin);
        boolean reprograma=!Objects.equals(actual.getFechaHora(),periodo.inicio())
                || !Objects.equals(actual.getFechaFin(),periodo.fin())
                || !Objects.equals(actual.getMascotaId(),cambios.getMascotaId())
                || !Objects.equals(actual.getMedicoId(),cambios.getMedicoId())
                || !Objects.equals(actual.getClienteId(),cambios.getClienteId());
        int atencion=datos.estadoAtencion(actual.getId());
        if (atencion!=0 && (reprograma || siguiente.equals("cancelada"))) {
            throw conflicto("La cita tiene una atención clínica y sus datos de reserva están protegidos.");
        }
        if (atencion==1 && siguiente.equals("finalizada")) {
            throw conflicto("Completa la atención clínica antes de finalizar la cita.");
        }
        boolean cambiaMotivo=!Objects.equals(actual.getMotivo(),cambios.getMotivo());
        if (!reserva(siguiente) && (reprograma || cambiaMotivo)) {
            throw invalido("Para cerrar la cita, conserva sus datos originales.");
        }
        // Cerrar o anotar citas previas sigue siendo posible si su franja ya no existe.
        boolean verificarReserva=reserva(siguiente) && (reprograma || !anterior.equals(siguiente));
        bloquearRecursos(actual,cambios,verificarReserva);
        if (verificarReserva) {
            validarFutura(periodo.inicio());
            agenda.validarReserva(cambios.getMedicoId(),cambios.getMascotaId(),actual.getId(),periodo);
        }
        if (!reprograma && !cambiaMotivo && actual.getEstado().equals(siguiente)) return actual;

        Foto foto=Foto.de(actual);
        actual.setClienteId(cambios.getClienteId());
        actual.setMascotaId(cambios.getMascotaId());
        actual.setMedicoId(cambios.getMedicoId());
        actual.setFechaHora(periodo.inicio());
        if (reprograma || cambios.fechaFinRegistrada()!=null) actual.setFechaFin(periodo.fin());
        actual.setEstado(siguiente);
        actual.setMotivo(cambios.getMotivo());
        Cita guardada=citas.saveAndFlush(actual);
        registrar(guardada,foto);
        return guardada;
    }

    private void bloquearRecursos(Cita anterior,Cita nueva,boolean exigeActivo) {
        // Todas las escrituras toman los médicos y después las mascotas, en orden de ID.
        // READ COMMITTED vuelve a leer los intervalos después de adquirir los bloqueos.
        for (int medico:Stream.of(anterior.getMedicoId(),nueva.getMedicoId()).distinct().sorted().toList()) {
            boolean activo=datos.medicoActivo(medico,true);
            if (exigeActivo && medico==nueva.getMedicoId() && !activo) throw conflicto("El médico seleccionado está inactivo.");
        }
        for (int mascota:Stream.of(anterior.getMascotaId(),nueva.getMascotaId()).distinct().sorted().toList()) {
            int propietario=datos.propietario(mascota);
            if (mascota==nueva.getMascotaId() && propietario!=nueva.getClienteId()) {
                throw invalido("La mascota no pertenece al cliente seleccionado.");
            }
        }
        if (!datos.clienteExiste(nueva.getClienteId())) throw invalido("El cliente seleccionado no existe.");
    }

    private static void validarCampos(Cita cita) {
        if (cita==null || cita.getClienteId()==null || cita.getClienteId()<=0
                || cita.getMascotaId()==null || cita.getMascotaId()<=0
                || cita.getMedicoId()==null || cita.getMedicoId()<=0 || cita.getFechaHora()==null) {
            throw invalido("Selecciona cliente, mascota, médico y fecha de inicio.");
        }
        if (cita.getMotivo()!=null && cita.getMotivo().length()>5000) throw invalido("El motivo supera 5000 caracteres.");
    }

    private static void validarFutura(LocalDateTime inicio) {
        if (inicio.isBefore(ahora())) throw invalido("No puedes agendar una cita en el pasado.");
    }

    static void validarTransicion(String anterior,String siguiente) {
        var usuario=identidad();
        if (usuario!=null && usuario.rol()==Rol.RECEPCION && ("atendida".equals(siguiente) || "finalizada".equals(siguiente))) {
            throw prohibido("Recepción no puede marcar atención ni finalizar citas.");
        }
        if (reserva(anterior) && reserva(siguiente)) return;
        if (reserva(anterior) && ("cancelada".equals(siguiente) || "atendida".equals(siguiente))) return;
        if ("atendida".equals(anterior) && "finalizada".equals(siguiente)) return;
        throw conflicto("El estado actual no permite esa transición.");
    }

    private void registrar(Cita cita,Foto antes) {
        var usuario=identidad();
        datos.registrarHistorial(cita.getId(),usuario==null ? null : usuario.usuarioId(),ahora(),antes,Foto.de(cita));
    }

    private record Foto(Integer medicoId,Integer mascotaId,Integer clienteId,LocalDateTime inicio,
            LocalDateTime fin,String estado,String motivo) {
        static Foto de(Cita c) {
            return new Foto(c.getMedicoId(),c.getMascotaId(),c.getClienteId(),c.getFechaHora(),c.getFechaFin(),
                    AgendaServicio.estado(c.getEstado()),c.getMotivo());
        }
    }
}
