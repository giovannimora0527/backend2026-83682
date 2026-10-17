package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.CitaParcialRq;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Pruebas académicas: reglas de negocio de la agenda sin base de datos real. */
class CitaParcialServiceImplTest {
    CitaRepository citas = mock(CitaRepository.class);
    ClienteRepository clientes = mock(ClienteRepository.class);
    MascotaRepository mascotas = mock(MascotaRepository.class);
    MedicoRepository medicos = mock(MedicoRepository.class);
    CitaParcialServiceImpl servicio;
    Cliente cliente = new Cliente();
    Mascota mascota = new Mascota();
    Medico medico = new Medico();

    @BeforeEach void iniciar() {
        servicio = new CitaParcialServiceImpl(citas, clientes, mascotas, medicos);
        cliente.setClienteId(1L);
        mascota.setMascotaId(2); mascota.setCliente(cliente);
        medico.setId(3L);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotas.findById(2)).thenReturn(Optional.of(mascota));
        when(medicos.findById(3L)).thenReturn(Optional.of(medico));
        when(citas.findAll()).thenReturn(List.of());
    }

    @Test void rechazaMascotaDeOtroCliente() {
        Cliente otro = new Cliente(); otro.setClienteId(99L); mascota.setCliente(otro);
        CitaParcialRq rq = datos(LocalDateTime.now().plusDays(2));
        assertThrows(ResponseStatusException.class, () -> servicio.guardar(rq));
        verify(citas, never()).save(any());
    }

    @Test void rechazaVeterinarioOcupadoEnVentanaQuinceMinutos() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2);
        Cita ocupada = new Cita(); ocupada.setId(40L); ocupada.setCliente(cliente);
        ocupada.setMascota(mascota); ocupada.setMedico(medico);
        ocupada.setEstado("Programada"); ocupada.setFechaHora(fecha.minusMinutes(10));
        when(citas.findAll()).thenReturn(List.of(ocupada));
        assertThrows(ResponseStatusException.class, () -> servicio.guardar(datos(fecha)));
    }

    @Test void permiteCitaSinConflicto() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2);
        when(citas.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("Programada", servicio.guardar(datos(fecha)).getEstado());
    }

    private CitaParcialRq datos(LocalDateTime fecha) {
        return new CitaParcialRq(1L, 2, 3L, fecha, "Vacunación", "Programada");
    }
}
