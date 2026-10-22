package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnotacionHistoriaServiceImplTests {
    @Mock
    private HistoriaMedicaRepository historiaMedicaRepository;
    @Mock
    private AnotacionHistoriaRepository anotacionHistoriaRepository;
    @Mock
    private MascotaRepository mascotaRepository;
    @Mock
    private MedicoRepository medicoRepository;
    @InjectMocks
    private AnotacionHistoriaServiceImpl anotacionHistoriaService;

    @Test
    void creaAnotacionConDiagnosticoYObservaciones() {
        Mascota mascota = new Mascota();
        HistoriaMedica historia = new HistoriaMedica();
        Medico medico = new Medico();
        AnotacionHistoriaRq request = new AnotacionHistoriaRq();
        request.setMascotaId(1);
        request.setMedicoId(2L);
        request.setDescripcion("Consulta");
        request.setDiagnostico("Dermatitis");
        request.setObservaciones("Control en una semana");
        when(mascotaRepository.findById(1)).thenReturn(Optional.of(mascota));
        when(historiaMedicaRepository.findByMascota(mascota)).thenReturn(Optional.of(historia));
        when(medicoRepository.findById(2L)).thenReturn(Optional.of(medico));
        when(anotacionHistoriaRepository.save(any(AnotacionHistoria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        anotacionHistoriaService.crearAnotacionHistoria(request);

        ArgumentCaptor<AnotacionHistoria> anotacionCaptor = ArgumentCaptor.forClass(AnotacionHistoria.class);
        verify(anotacionHistoriaRepository).save(anotacionCaptor.capture());
        AnotacionHistoria anotacion = anotacionCaptor.getValue();
        assertSame(historia, anotacion.getHistoria());
        assertSame(medico, anotacion.getMedico());
        assertEquals("Consulta", anotacion.getDescripcion());
        assertEquals("Dermatitis", anotacion.getDiagnostico());
        assertEquals("Control en una semana", anotacion.getObservaciones());
    }
}
