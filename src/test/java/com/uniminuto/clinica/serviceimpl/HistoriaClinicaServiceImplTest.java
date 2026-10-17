package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaRequest;
import com.uniminuto.clinica.repository.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoriaClinicaServiceImplTest {
    @Mock HistoriaClinicaRepository historias;
    @Mock ClienteRepository clientes;
    @Mock MascotaRepository mascotas;
    @Mock CitaRepository citas;
    @Mock VeterinarioRepository veterinarios;
    @Mock AnotacionRepository anotaciones;
    @InjectMocks HistoriaClinicaServiceImpl service;

    @Test
    void creaHistoriaConCitaAtendidaYRelacionesCoherentes() {
        Cliente cliente = cliente(); Mascota mascota = mascota(cliente); Veterinario vet = veterinario();
        Cita cita = new Cita(); cita.setCitaId(4L); cita.setCliente(cliente); cita.setMascota(mascota); cita.setVeterinario(vet); cita.setEstado(EstadoCita.ATENDIDA);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotas.findById(2)).thenReturn(Optional.of(mascota));
        when(veterinarios.findById(3L)).thenReturn(Optional.of(vet));
        when(citas.findById(4L)).thenReturn(Optional.of(cita));
        when(historias.existsByCita_CitaId(4L)).thenReturn(false);
        when(historias.save(any())).thenAnswer(call -> {
            HistoriaClinica saved = call.getArgument(0);
            saved.setHistoriaClinicaId(77L);
            return saved;
        });
        when(anotaciones.save(any())).thenAnswer(call -> call.getArgument(0));

        var result = service.crear(new HistoriaRequest(1L, 2, 4L, 3L, LocalDateTime.now(), "Control", "Estable", null));

        assertEquals(77L, result.historiaClinicaId());
        assertEquals(4L, result.citaId());
        assertEquals("Luna", result.mascotaNombre());
        ArgumentCaptor<HistoriaClinica> historyCaptor = ArgumentCaptor.forClass(HistoriaClinica.class);
        verify(historias).save(historyCaptor.capture());
        ArgumentCaptor<Anotacion> noteCaptor = ArgumentCaptor.forClass(Anotacion.class);
        verify(anotaciones, times(1)).save(noteCaptor.capture());
        HistoriaClinicaNarrativeCodec.Narrative stored = HistoriaClinicaNarrativeCodec.decode(
                noteCaptor.getValue().getDescripcion());
        assertEquals("Control", stored.motivoConsulta());
        assertEquals("Estable", stored.diagnostico());
        assertNull(stored.observaciones());

        when(historias.findAll(org.mockito.ArgumentMatchers.<Specification<HistoriaClinica>>any(),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(historyCaptor.getValue()), PageRequest.of(0, 20), 1));
        when(historias.findById(77L)).thenReturn(Optional.of(historyCaptor.getValue()));
        when(anotaciones.findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(77L))
                .thenReturn(List.of(noteCaptor.getValue()));

        var listed = service.listar(0, 20, null, "DESC", null, null, null, null);
        Long idFromList = listed.content().get(0).historiaClinicaId();
        var detail = service.consultar(idFromList);

        assertEquals(result.historiaClinicaId(), idFromList);
        assertEquals("Control", detail.motivoConsulta());
        assertEquals("Estable", detail.diagnostico());
        verify(historias).findById(idFromList);
        verify(anotaciones, atLeastOnce())
                .findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(idFromList);
    }

    @Test
    void recuperaPorSeparadoLosCamposClinicosDesdeUnaHistoriaPersistida() {
        Cliente cliente = cliente();
        Mascota mascota = mascota(cliente);
        Veterinario vet = veterinario();
        HistoriaClinica persisted = persistedHistory(12L, mascota);
        Anotacion note = new Anotacion();
        note.setHistoriaClinica(persisted);
        note.setVeterinario(vet);
        note.setFechaHora(persisted.getFechaHora());
        note.setDescripcion(HistoriaClinicaNarrativeCodec.encode(
                "Dolor abdominal", "Gastritis", "Dieta blanda por tres días"));
        when(historias.findById(12L)).thenReturn(Optional.of(persisted));
        when(anotaciones.findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(12L))
                .thenReturn(List.of(note));

        var result = service.consultar(12L);

        assertEquals(1L, result.clienteId());
        assertEquals(3L, result.veterinarioId());
        assertEquals("Dolor abdominal", result.motivoConsulta());
        assertEquals("Gastritis", result.diagnostico());
        assertEquals("Dieta blanda por tres días", result.observaciones());
        assertNull(result.citaId());
        verify(anotaciones).findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(12L);
    }

    @Test
    void listadoRecuperaElMotivoDesdeElFormatoEstructurado() {
        Mascota mascota = mascota(cliente());
        HistoriaClinica persisted = persistedHistory(13L, mascota);
        Anotacion note = new Anotacion();
        note.setHistoriaClinica(persisted);
        note.setVeterinario(veterinario());
        note.setDescripcion(HistoriaClinicaNarrativeCodec.encode("Control anual", "Sano", null));
        when(historias.findAll(org.mockito.ArgumentMatchers.<Specification<HistoriaClinica>>any(),
                org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(persisted), PageRequest.of(0, 20), 1));
        when(anotaciones.findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(13L))
                .thenReturn(List.of(note));

        var result = service.listar(0, 20, null, "DESC", null, null, null, null);

        assertEquals(1, result.totalElements());
        assertEquals("Control anual", result.content().get(0).motivoConsulta());
    }

    @Test
    void conservaLaAnotacionAntiguaComoTextoPlanoSinReinterpretarla() {
        Mascota mascota = mascota(cliente());
        HistoriaClinica persisted = persistedHistory(14L, mascota);
        Anotacion legacy = new Anotacion();
        legacy.setHistoriaClinica(persisted);
        legacy.setVeterinario(veterinario());
        String oldText = "Motivo: control\nDiagnostico previo: estable\nObservaciones previas";
        legacy.setDescripcion(oldText);
        when(historias.findById(14L)).thenReturn(Optional.of(persisted));
        when(anotaciones.findByHistoriaClinica_HistoriaClinicaIdOrderByFechaHoraDesc(14L))
                .thenReturn(List.of(legacy));

        var result = service.consultar(14L);

        assertEquals(oldText, result.motivoConsulta());
        assertNull(result.diagnostico());
        assertNull(result.observaciones());
        assertEquals(oldText, HistoriaClinicaNarrativeCodec.display(oldText));
    }

    @Test
    void rechazaHistoriaSiMascotaNoPerteneceAlCliente() {
        Cliente clienteElegido = cliente(); Cliente otro = new Cliente(); otro.setClienteId(7L);
        when(clientes.findById(1L)).thenReturn(Optional.of(clienteElegido));
        when(mascotas.findById(2)).thenReturn(Optional.of(mascota(otro)));

        BadRequestException error = assertThrows(BadRequestException.class, () ->
                service.crear(new HistoriaRequest(1L, 2, null, 3L, LocalDateTime.now(), "Control", "Diagnóstico", null)));

        assertEquals(409, error.getStatus().value());
        verifyNoInteractions(veterinarios, citas);
        verify(historias, never()).save(any());
    }

    private Cliente cliente() { Cliente c=new Cliente(); c.setClienteId(1L); c.setNombres("Ana"); c.setApellidos("Lopez"); return c; }
    private Mascota mascota(Cliente c) { Mascota m=new Mascota(); m.setMascotaId(2); m.setNombreMascota("Luna"); m.setCliente(c); return m; }
    private Veterinario veterinario() { Veterinario v=new Veterinario(); v.setVeterinarioId(3L); v.setNombres("Luis"); v.setApellidos("Vega"); return v; }
    private HistoriaClinica persistedHistory(Long id, Mascota mascota) {
        HistoriaClinica history = new HistoriaClinica();
        history.setHistoriaClinicaId(id);
        history.setMascota(mascota);
        history.setFechaHora(LocalDateTime.of(2026, 10, 16, 10, 30));
        return history;
    }
}
