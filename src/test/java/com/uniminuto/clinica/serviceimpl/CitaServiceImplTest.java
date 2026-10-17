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
    void rechazaCitasEnElPasado() {
        CitaRequest request = new CitaRequest(1L, 2, 3L, LocalDate.now().minusDays(1), LocalTime.NOON, "Control", null);
        assertThrows(BadRequestException.class, () -> service.crear(request));
        verifyNoInteractions(clientes, mascotas, veterinarios, citas);
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
