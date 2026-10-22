package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicoServiceImplTests {

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoServiceImpl medicoService;

    private final LocalDateTime fechaInicio = LocalDateTime.of(2026, 10, 15, 8, 0);
    private final LocalDateTime fechaFin = fechaInicio.plusHours(1);

    @Test
    void consultaDisponiblesUnaSolaVez() {
        Medico medico = new Medico();
        medico.setId(1L);
        List<Medico> disponibles = List.of(medico);
        when(medicoRepository.findDisponiblesPorFecha(fechaInicio, fechaFin, "PROGRAMADA"))
                .thenReturn(disponibles);

        assertEquals(disponibles, medicoService.listarMedicosDisponiblesPorFecha(fechaInicio, fechaFin));

        verify(medicoRepository).findDisponiblesPorFecha(fechaInicio, fechaFin, "PROGRAMADA");
        verifyNoMoreInteractions(medicoRepository);
    }

    @Test
    void retornaListaVaciaCuandoNoHayDisponibles() {
        when(medicoRepository.findDisponiblesPorFecha(fechaInicio, fechaFin, "PROGRAMADA"))
                .thenReturn(List.of());

        assertEquals(List.of(), medicoService.listarMedicosDisponiblesPorFecha(fechaInicio, fechaFin));
    }

    @Test
    void permiteConsultarUnInstante() {
        when(medicoRepository.findDisponiblesPorFecha(fechaInicio, fechaInicio, "PROGRAMADA"))
                .thenReturn(List.of());

        assertEquals(List.of(), medicoService.listarMedicosDisponiblesPorFecha(fechaInicio, fechaInicio));
        verify(medicoRepository).findDisponiblesPorFecha(fechaInicio, fechaInicio, "PROGRAMADA");
    }

    @Test
    void rechazaFechasNulasSinConsultar() {
        assertThrows(BadRequestException.class,
                () -> medicoService.listarMedicosDisponiblesPorFecha(null, fechaFin));
        assertThrows(BadRequestException.class,
                () -> medicoService.listarMedicosDisponiblesPorFecha(fechaInicio, null));
        verifyNoInteractions(medicoRepository);
    }

    @Test
    void rechazaRangoInvertidoSinConsultar() {
        assertThrows(BadRequestException.class,
                () -> medicoService.listarMedicosDisponiblesPorFecha(fechaFin, fechaInicio));
        verifyNoInteractions(medicoRepository);
    }
}
