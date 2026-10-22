package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceImplTests {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private CitaServiceImpl citaService;

    @ParameterizedTest
    @ValueSource(longs = {1, 1440})
    void rechazaCrearCitaEnElPasado(long minutos) {
        CitaRq solicitud = solicitudPara(LocalDateTime.now().minusMinutes(minutos));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> citaService.crearCita(solicitud));

        assertEquals("La fecha y hora de la cita no puede ser anterior a la fecha y hora actual",
                error.getMessage());
        verifyNoInteractions(citaRepository, mascotaRepository, medicoRepository);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 1440})
    void rechazaActualizarCitaEnElPasado(long minutos) {
        CitaRq solicitud = solicitudPara(LocalDateTime.now().minusMinutes(minutos));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> citaService.actualizarCita(solicitud));

        assertEquals("La fecha y hora de la cita no puede ser anterior a la fecha y hora actual",
                error.getMessage());
        verifyNoInteractions(citaRepository, mascotaRepository, medicoRepository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void permiteGuardarCitaFutura(boolean actualizar) {
        CitaRq solicitud = solicitudPara(LocalDateTime.now().plusDays(1));
        when(mascotaRepository.findById(solicitud.getMascotaId()))
                .thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(solicitud.getMedicoId()))
                .thenReturn(Optional.of(new Medico()));

        if (actualizar) {
            Cita existente = new Cita();
            existente.setFechaHora(solicitud.getFechaHora().plusDays(1));
            when(citaRepository.findById(solicitud.getId())).thenReturn(Optional.of(existente));
            citaService.actualizarCita(solicitud);
        } else {
            citaService.crearCita(solicitud);
        }

        verify(citaRepository).save(any(Cita.class));
    }

    @ParameterizedTest
    @CsvSource({
            "CANCELADA, true",
            "CONFIRMADA, true",
            "CANCELADA, false",
            "CONFIRMADA, false"
    })
    void omiteValidacionesDeRangoAlCancelarOConfirmar(String estado, boolean mismaFecha) {
        CitaRq solicitud = solicitudPara(LocalDateTime.now().plusDays(1));
        solicitud.setEstado(estado);
        Cita existente = new Cita();
        existente.setEstado("PROGRAMADA");
        existente.setFechaHora(mismaFecha ? solicitud.getFechaHora() : solicitud.getFechaHora().plusDays(1));
        when(mascotaRepository.findById(solicitud.getMascotaId()))
                .thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(solicitud.getMedicoId()))
                .thenReturn(Optional.of(new Medico()));
        when(citaRepository.findById(solicitud.getId())).thenReturn(Optional.of(existente));

        citaService.actualizarCita(solicitud);

        assertEquals(estado, existente.getEstado());
        verify(citaRepository).findById(solicitud.getId());
        verify(citaRepository).save(existente);
        verifyNoMoreInteractions(citaRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROGRAMADA", "CANCELADA", "CONFIRMADA"})
    void rechazaActualizarEnElPasadoParaCualquierEstado(String estado) {
        CitaRq solicitud = solicitudPara(LocalDateTime.now().minusMinutes(1));
        solicitud.setEstado(estado);

        assertThrows(BadRequestException.class, () -> citaService.actualizarCita(solicitud));

        verifyNoInteractions(citaRepository, mascotaRepository, medicoRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PROGRAMADA", "CANCELADA", "CONFIRMADA"})
    void validaRangoSiElEstadoSolicitadoEsProgramada(String estadoAnterior) {
        CitaRq solicitud = solicitudPara(LocalDateTime.now().plusDays(1));
        Cita existente = new Cita();
        existente.setEstado(estadoAnterior);
        existente.setFechaHora(solicitud.getFechaHora().plusDays(1));
        Medico medico = new Medico();
        when(mascotaRepository.findById(solicitud.getMascotaId()))
                .thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(solicitud.getMedicoId())).thenReturn(Optional.of(medico));
        when(citaRepository.findById(solicitud.getId())).thenReturn(Optional.of(existente));
        when(citaRepository.findByMedicoAndFechaHoraBetween(medico,
                solicitud.getFechaHora().minusMinutes(30), solicitud.getFechaHora().plusMinutes(30)))
                .thenReturn(List.of(new Cita()));

        assertThrows(BadRequestException.class, () -> citaService.actualizarCita(solicitud));

        verify(citaRepository).findById(solicitud.getId());
        verify(citaRepository).findByMedicoAndFechaHoraBetween(medico,
                solicitud.getFechaHora().minusMinutes(30), solicitud.getFechaHora().plusMinutes(30));
        verifyNoMoreInteractions(citaRepository);
    }

    private CitaRq solicitudPara(LocalDateTime fechaHora) {
        CitaRq solicitud = new CitaRq();
        solicitud.setId(1L);
        solicitud.setMascotaId(1);
        solicitud.setMedicoId(1L);
        solicitud.setFechaHora(fechaHora);
        solicitud.setMotivo("Consulta");
        solicitud.setEstado("PROGRAMADA");
        return solicitud;
    }
}
