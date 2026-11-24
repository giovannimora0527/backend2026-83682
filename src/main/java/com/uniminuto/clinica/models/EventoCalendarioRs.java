package com.uniminuto.clinica.models;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import java.time.LocalDateTime;

/** Evento para FullCalendar (C6). */
public record EventoCalendarioRs(Long id, String title, LocalDateTime start, LocalDateTime end, EstadoCita estado,
                                 String color, Propiedades extendedProps) {

    public record Propiedades(String motivo, Integer mascotaId, String mascotaNombre, String especie, String raza,
                              Integer edad, Long clienteId, String clienteNombre, String clienteTelefono,
                              String clienteDocumento, Long medicoId, String medicoNombre, Long servicioId,
                              String servicioNombre, String motivoCancelacion) {
    }

    public static EventoCalendarioRs de(Cita c) {
        Mascota mascota = c.getMascota();
        Cliente cliente = c.getCliente();
        Medico medico = c.getMedico();
        Raza raza = mascota.getRaza();
        Servicio servicio = c.getServicio();
        String propietario = cliente.getNombres() + " " + cliente.getApellidos();
        return new EventoCalendarioRs(c.getId(), mascota.getNombreMascota() + " - " + propietario,
                c.getFechaInicio(), c.getFechaFin(), c.getEstado(), c.getEstado().getColor(),
                new Propiedades(c.getMotivo(), mascota.getMascotaId(), mascota.getNombreMascota(),
                        raza == null ? null : raza.getEspecie(), raza == null ? null : raza.getNombre(),
                        mascota.getEdad(), cliente.getId(), propietario, cliente.getTelefono(),
                        cliente.getNumeroDocumento(), medico.getId(),
                        medico.getNombres() + " " + medico.getApellidos(),
                        servicio == null ? null : servicio.getId(), servicio == null ? null : servicio.getNombre(),
                        c.getMotivoCancelacion()));
    }
}
