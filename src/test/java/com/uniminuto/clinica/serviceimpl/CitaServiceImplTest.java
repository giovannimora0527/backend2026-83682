package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRequest;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaServiceImplTest {
    @Mock CitaRepository citas;
    @Mock ClienteRepository clientes;
    @Mock MascotaRepository mascotas;
    @Mock VeterinarioRepository veterinarios;
    @InjectMocks CitaServiceImpl service;

    @Test
    void creaCitaValidaConEstadoPendientePorDefecto() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotas.lockById(2)).thenReturn(Optional.of(mascota));
        when(veterinarios.lockById(3L)).thenReturn(Optional.of(vet));
        when(citas.findByVeterinario_VeterinarioIdAndFecha(eq(3L), any())).thenReturn(List.of());
        when(citas.findByMascota_MascotaIdAndFecha(eq(2), any())).thenReturn(List.of());
        when(citas.save(any(Cita.class))).thenAnswer(call -> call.getArgument(0));

        var response = service.crear(new CitaRequest(1L, 2, 3L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Control", null));

        assertEquals(EstadoCita.PENDIENTE, response.estado());
        assertEquals("Luna", response.mascotaNombre());
        verify(citas).save(any(Cita.class));
    }

    @Test
    void rechazaCitaConMenosDeQuinceMinutosDeSeparacion() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        LocalDate date = LocalDate.now().plusDays(1);
        Cita existing = new Cita(); existing.setCitaId(44L); existing.setVeterinario(vet); existing.setMascota(mascota);
        existing.setFecha(date); existing.setHora(LocalTime.of(10, 0)); existing.setEstado(EstadoCita.CONFIRMADA);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotas.lockById(2)).thenReturn(Optional.of(mascota));
        when(veterinarios.lockById(3L)).thenReturn(Optional.of(vet));
        when(citas.findByVeterinario_VeterinarioIdAndFecha(3L, date)).thenReturn(List.of(existing));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> service.crear(new CitaRequest(1L, 2, 3L, date, LocalTime.of(10, 10), "Control", EstadoCita.PENDIENTE)));

        assertEquals(409, error.getStatus().value());
        verify(citas, never()).save(any());
    }

    @Test
    void rechazaConflictoDelVeterinarioQueCruzaLaMedianoche() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        LocalDate dia = LocalDate.now().plusDays(2);
        Cita nocturna = new Cita(); nocturna.setCitaId(45L); nocturna.setVeterinario(vet); nocturna.setMascota(mascota(cliente));
        nocturna.setFechaHora(dia.minusDays(1).atTime(23, 55)); nocturna.setEstado(EstadoCita.PROGRAMADA);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotas.lockById(2)).thenReturn(Optional.of(mascota));
        when(veterinarios.lockById(3L)).thenReturn(Optional.of(vet));
        when(citas.findByVeterinario_VeterinarioIdAndFecha(3L, dia)).thenReturn(List.of());
        when(citas.findByVeterinario_VeterinarioIdAndFecha(3L, dia.minusDays(1))).thenReturn(List.of(nocturna));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> service.crear(new CitaRequest(1L, 2, 3L, dia, LocalTime.of(0, 5), "Urgencia", null)));

        assertEquals(409, error.getStatus().value());
        verify(citas, never()).save(any());
    }

    @Test
    void rechazaCitasEnElPasado() {
        CitaRequest request = new CitaRequest(1L, 2, 3L, LocalDate.now().minusDays(1), LocalTime.NOON, "Control", null);
        assertThrows(BadRequestException.class, () -> service.crear(request));
        verifyNoInteractions(clientes, mascotas, veterinarios, citas);
    }

    @Test
    void actualizaCitaYaPasadaSiNoCambiaSuFechaNiSuHora() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        LocalDate ayer = LocalDate.now().minusDays(1);
        Cita existente = cita(cliente, mascota, vet, ayer, EstadoCita.PENDIENTE);
        prepararActualizacion(existente, cliente, mascota, vet, ayer);

        var response = service.actualizar(4L, new CitaRequest(1L, 2, 3L, ayer, LocalTime.of(11, 30), "Control actualizado", EstadoCita.CONFIRMADA));

        assertEquals(EstadoCita.CONFIRMADA, response.estado());
        assertEquals("Control actualizado", response.motivoConsulta());
        verify(citas).save(existente);
    }

    @Test
    void noPermiteMoverUnaCitaAUnaFechaPasada() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        Cita existente = cita(cliente, mascota, vet, LocalDate.now().plusDays(2), EstadoCita.PENDIENTE);
        when(citas.findById(4L)).thenReturn(Optional.of(existente));

        assertThrows(BadRequestException.class, () -> service.actualizar(4L,
                new CitaRequest(1L, 2, 3L, LocalDate.now().minusDays(1), LocalTime.of(11, 30), "Control", EstadoCita.PENDIENTE)));
        verify(citas, never()).save(any());
    }

    @Test
    void citaProgramadaPasadaPuedeMarcarseComoAtendida() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        LocalDate fecha = LocalDate.now().minusDays(30);
        Cita existente = cita(cliente, mascota, vet, fecha, EstadoCita.PROGRAMADA);
        prepararActualizacion(existente, cliente, mascota, vet, fecha);

        var response = service.actualizar(4L, new CitaRequest(1L, 2, 3L, fecha, LocalTime.of(11, 30), "Control", EstadoCita.ATENDIDA));

        assertEquals(EstadoCita.ATENDIDA, response.estado());
    }

    @Test
    void citaProgramadaPuedeCambiarVeterinarioFechaYHora() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        Veterinario otroVet = new Veterinario(); otroVet.setVeterinarioId(7L); otroVet.setNombres("Rosa"); otroVet.setApellidos("Díaz");
        LocalDate nuevaFecha = LocalDate.now().plusDays(3);
        Cita existente = cita(cliente, mascota, vet, LocalDate.now().plusDays(1), EstadoCita.PROGRAMADA);
        when(citas.findById(4L)).thenReturn(Optional.of(existente));
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotas.lockById(2)).thenReturn(Optional.of(mascota));
        when(veterinarios.lockById(7L)).thenReturn(Optional.of(otroVet));
        when(citas.findByVeterinario_VeterinarioIdAndFecha(7L, nuevaFecha)).thenReturn(List.of());
        when(citas.findByMascota_MascotaIdAndFecha(2, nuevaFecha)).thenReturn(List.of());
        when(citas.save(any(Cita.class))).thenAnswer(call -> call.getArgument(0));

        var response = service.actualizar(4L, new CitaRequest(1L, 2, 7L, nuevaFecha, LocalTime.of(9, 15), "Control", EstadoCita.PROGRAMADA));

        assertEquals(7L, response.veterinarioId());
        assertEquals(nuevaFecha, response.fecha());
        assertEquals(LocalTime.of(9, 15), response.hora());
        assertEquals(EstadoCita.PROGRAMADA, response.estado());
    }

    @Test
    void citasCanceladasYAtendidasNoSePuedenModificar() {
        for (EstadoCita estado : new EstadoCita[]{EstadoCita.CANCELADA, EstadoCita.ATENDIDA}) {
            Cliente cliente = cliente();
            Cita existente = cita(cliente, mascota(cliente), veterinario(), LocalDate.now().plusDays(1), estado);
            when(citas.findById(4L)).thenReturn(Optional.of(existente));
            BadRequestException error = assertThrows(BadRequestException.class, () -> service.actualizar(4L,
                    new CitaRequest(1L, 2, 3L, LocalDate.now().plusDays(1), LocalTime.of(11, 30), "Control", EstadoCita.PENDIENTE)));
            assertEquals(409, error.getStatus().value(), estado.name());
        }
        verify(citas, never()).save(any());
    }

    private Cita cita(Cliente cliente, Mascota mascota, Veterinario vet, LocalDate fecha, EstadoCita estado) {
        Cita c = new Cita(); c.setCitaId(4L); c.setCliente(cliente); c.setMascota(mascota); c.setVeterinario(vet);
        c.setFechaHora(fecha.atTime(11, 30)); c.setMotivoConsulta("Control"); c.setEstado(estado);
        return c;
    }

    private void prepararActualizacion(Cita existente, Cliente cliente, Mascota mascota, Veterinario vet, LocalDate fecha) {
        when(citas.findById(4L)).thenReturn(Optional.of(existente));
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotas.lockById(2)).thenReturn(Optional.of(mascota));
        when(veterinarios.lockById(3L)).thenReturn(Optional.of(vet));
        when(citas.findByVeterinario_VeterinarioIdAndFecha(3L, fecha)).thenReturn(List.of(existente));
        when(citas.findByMascota_MascotaIdAndFecha(2, fecha)).thenReturn(List.of(existente));
        when(citas.save(any(Cita.class))).thenAnswer(call -> call.getArgument(0));
    }

    private Cliente cliente() {
        Cliente c = new Cliente(); c.setClienteId(1L); c.setNombres("Ana"); c.setApellidos("Lopez"); return c;
    }
    private Mascota mascota(Cliente cliente) {
        Mascota m = new Mascota(); m.setMascotaId(2); m.setNombreMascota("Luna"); m.setCliente(cliente); return m;
    }
    private Veterinario veterinario() {
        Veterinario v = new Veterinario(); v.setVeterinarioId(3L); v.setNombres("Luis"); v.setApellidos("Vega"); return v;
    }
}
