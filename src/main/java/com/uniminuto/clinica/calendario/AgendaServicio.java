package com.uniminuto.clinica.calendario;

import static com.uniminuto.clinica.calendario.AgendaDatos.*;

import com.uniminuto.clinica.service.AlcanceMedico;
import com.uniminuto.clinica.service.IdentidadService;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AgendaServicio {
    public static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Set<String> ESTADOS=Set.of("pendiente","confirmada","cancelada","atendida","finalizada");
    private final AgendaRepositorio datos;
    private final AlcanceMedico alcance;

    public AgendaServicio(AgendaRepositorio datos, AlcanceMedico alcance) {
        this.datos=datos;
        this.alcance=alcance;
    }

    public static String estado(String valor) {
        String normal=valor==null ? "" : valor.strip().toLowerCase(Locale.ROOT);
        if (normal.equals("programada")) return "pendiente";
        if (!ESTADOS.contains(normal)) throw invalido("El estado de la cita no es válido.");
        return normal;
    }

    public static boolean reserva(String estado) { return "pendiente".equals(estado) || "confirmada".equals(estado); }

    public static LocalDateTime ahora() { return LocalDateTime.now(ZONA); }

    public static Identidad identidad() {
        var req=RequestContextHolder.getRequestAttributes();
        return req==null ? null : (Identidad)req.getAttribute(IdentidadService.ATRIBUTO,RequestAttributes.SCOPE_REQUEST);
    }

    public Configuracion configuracion(int medico) {
        alcance.exigirMedico(medico);
        return new Configuracion(datos.medicoActivo(medico,false),datos.horarios(medico),datos.ausencias(medico,null));
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Configuracion guardarHorarios(int medico,List<Franja> franjas) {
        alcance.exigirMedico(medico);
        datos.medicoActivo(medico,true);
        List<Franja> horarios=validarFranjas(franjas);
        for (var reserva:datos.reservas(medico,new Intervalo(ahora(),LocalDateTime.of(9999,12,31,0,0)))) {
            if (!dentro(reserva,horarios)) throw conflicto("Hay citas próximas fuera del nuevo horario. Reprográmalas primero.");
        }
        datos.reemplazarHorarios(medico,horarios);
        return configuracion(medico);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public Configuracion registrarAusencia(int medico,Ausencia ausencia) {
        alcance.exigirMedico(medico);
        datos.medicoActivo(medico,true);
        if (ausencia==null) throw invalido("Debes indicar la ausencia.");
        var periodo=intervalo(ausencia.inicio(),ausencia.fin());
        if (ausencia.motivo()==null || ausencia.motivo().isBlank() || ausencia.motivo().strip().length()>255) {
            throw invalido("Indica un motivo de hasta 255 caracteres.");
        }
        if (!datos.reservas(medico,periodo).isEmpty()) {
            throw conflicto("Reprograma o cancela las citas del período antes de registrar la ausencia.");
        }
        if (!datos.ausencias(medico,periodo).isEmpty()) throw conflicto("Ya hay una ausencia en ese período.");
        datos.agregarAusencia(medico,new Ausencia(null,periodo.inicio(),periodo.fin(),ausencia.motivo().strip()));
        return configuracion(medico);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public void cambiarActividad(int medico,boolean activo) {
        datos.medicoActivo(medico,true);
        if (!activo && !datos.reservas(medico,new Intervalo(ahora(),LocalDateTime.of(9999,12,31,0,0))).isEmpty()) {
            throw conflicto("Reprograma o cancela las citas próximas antes de inactivar al médico.");
        }
        datos.activar(medico,activo);
    }

    public Disponibilidad disponibilidad(int medico,LocalDate fecha) {
        alcance.exigirMedico(medico);
        boolean activo=datos.medicoActivo(medico,false);
        if (!activo) return new Disponibilidad(fecha,false,List.of());
        var dia=new Intervalo(fecha.atStartOfDay(),fecha.plusDays(1).atStartOfDay());
        var bloqueos=new ArrayList<>(datos.ocupados(medico,dia));
        datos.ausencias(medico,dia).forEach(a->bloqueos.add(new Intervalo(a.inicio(),a.fin())));
        bloqueos.sort(Comparator.comparing(Intervalo::inicio));
        List<Intervalo> libres=new ArrayList<>();
        for (Franja f:datos.horarios(medico)) {
            if (f.dia()!=fecha.getDayOfWeek().getValue()) continue;
            LocalDateTime cursor=fecha.atTime(f.inicio());
            LocalDateTime fin=fecha.atTime(f.fin());
            for (Intervalo bloqueo:bloqueos) {
                if (!bloqueo.fin().isAfter(cursor)) continue;
                if (!bloqueo.inicio().isBefore(fin)) break;
                if (bloqueo.inicio().isAfter(cursor)) libres.add(new Intervalo(cursor,bloqueo.inicio()));
                cursor=bloqueo.fin();
            }
            if (cursor.isBefore(fin)) libres.add(new Intervalo(cursor,fin));
        }
        return new Disponibilidad(fecha,true,libres);
    }

    public void validarReserva(int medico,int mascota,Long excluir,Intervalo periodo) {
        if (!dentro(periodo,datos.horarios(medico)) || !datos.ausencias(medico,periodo).isEmpty()) {
            throw conflicto("La cita está fuera del horario disponible del médico.");
        }
        if (datos.conflicto(excluir,"medico",medico,periodo)) throw conflicto("El médico ya tiene una cita en ese horario.");
        if (datos.conflicto(excluir,"mascota",mascota,periodo)) throw conflicto("La mascota ya tiene una cita en ese horario.");
    }

    public List<Evento> eventos(LocalDate desde, LocalDate hasta,String estado) {
        Integer medico=alcance.medicoId();
        if (medico==null) throw prohibido("El calendario personal requiere un usuario médico vinculado.");
        if (desde==null || hasta==null || hasta.isBefore(desde) || hasta.isAfter(desde.plusDays(365))) {
            throw invalido("Selecciona un rango de hasta 366 días.");
        }
        return datos.eventos(medico,new Intervalo(desde.atStartOfDay(),hasta.plusDays(1).atStartOfDay()),
                estado==null || estado.isBlank() ? null : estado(estado));
    }

    public List<Historial> historial(long cita) { return datos.historial(cita); }

    public static Intervalo intervalo(LocalDateTime inicio,LocalDateTime fin) {
        if (inicio==null || fin==null || !fin.isAfter(inicio) || inicio.getYear()<1000 || fin.getYear()>9999) {
            throw invalido("La hora final debe ser posterior a la inicial.");
        }
        return new Intervalo(inicio,fin);
    }

    static List<Franja> validarFranjas(List<Franja> franjas) {
        if (franjas==null || franjas.size()>70) throw invalido("Indica hasta 70 franjas semanales.");
        for (Franja f:franjas) {
            if (f==null || f.dia()<1 || f.dia()>7 || f.inicio()==null || f.fin()==null
                    || !f.fin().isAfter(f.inicio()) || f.inicio().getNano()!=0 || f.fin().getNano()!=0) {
                throw invalido("Cada franja necesita día y un fin posterior al inicio.");
            }
        }
        var ordenadas=franjas.stream().sorted(Comparator.comparingInt(Franja::dia).thenComparing(Franja::inicio)).toList();
        for (int i=1;i<ordenadas.size();i++) {
            Franja anterior=ordenadas.get(i-1), actual=ordenadas.get(i);
            if (anterior.dia()==actual.dia() && actual.inicio().isBefore(anterior.fin())) {
                throw invalido("Las franjas del mismo día no pueden superponerse.");
            }
        }
        return ordenadas;
    }

    static boolean dentro(Intervalo intervalo,List<Franja> franjas) {
        var inicio=intervalo.inicio();
        return inicio.toLocalDate().equals(intervalo.fin().toLocalDate()) && franjas.stream().anyMatch(f->
                f.dia()==inicio.getDayOfWeek().getValue() && !inicio.toLocalTime().isBefore(f.inicio())
                        && !intervalo.fin().toLocalTime().isAfter(f.fin()));
    }

    public static ResponseStatusException invalido(String mensaje) { return error(HttpStatus.BAD_REQUEST,mensaje); }
    public static ResponseStatusException conflicto(String mensaje) { return error(HttpStatus.CONFLICT,mensaje); }
    public static ResponseStatusException prohibido(String mensaje) { return error(HttpStatus.FORBIDDEN,mensaje); }
    public static ResponseStatusException noExiste(String elemento) { return error(HttpStatus.NOT_FOUND,elemento+" no encontrado."); }
    private static ResponseStatusException error(HttpStatus estado,String mensaje) {
        return new ResponseStatusException(estado,mensaje);
    }
}
