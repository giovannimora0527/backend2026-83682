package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoriaServiceImplTests {
    @Mock
    private HistoriaMedicaRepository historiaMedicaRepository;
    @Mock
    private AnotacionHistoriaRepository anotacionHistoriaRepository;
    @Mock
    private FormulaMedicaRepository formulaMedicaRepository;
    @InjectMocks
    private HistoriaServiceImpl historiaService;

    @Test
    void incluyeFormulasSoloDeLaCitaDeCadaAnotacion() {
        HistoriaMedica historia = new HistoriaMedica();
        historia.setId(1L);
        HistoriaMedica otraHistoria = new HistoriaMedica();
        otraHistoria.setId(2L);
        AnotacionHistoria primera = anotacion(historia, 10L);
        AnotacionHistoria segunda = anotacion(historia, 20L);
        AnotacionHistoria compartida = anotacion(otraHistoria, 10L);
        FormulaMedica formula = formula(primera.getCita());
        FormulaMedica otraFormula = formula(primera.getCita());
        FormulaMedica formulaSegunda = formula(segunda.getCita());
        when(historiaMedicaRepository.findAll()).thenReturn(List.of(historia, otraHistoria));
        when(anotacionHistoriaRepository.findByHistoria(historia)).thenReturn(List.of(primera, segunda));
        when(anotacionHistoriaRepository.findByHistoria(otraHistoria)).thenReturn(List.of(compartida));
        when(formulaMedicaRepository.findByCita_IdInOrderByFechaCreacionRegistroDesc(List.of(10L, 20L)))
                .thenReturn(List.of(formula, otraFormula, formulaSegunda));

        List<HistoriaMedicaRs> resultado = historiaService.getAllHistorias();

        AnotacionHistoriaRs anotacion = resultado.getFirst().getAnotaciones().getFirst();
        assertSame(historia, resultado.getFirst().getHistoria());
        assertEquals(primera.getId(), anotacion.getId());
        assertSame(historia, anotacion.getHistoria());
        assertSame(primera.getMedico(), anotacion.getMedico());
        assertSame(primera.getCita(), anotacion.getCita());
        assertEquals(primera.getFecha(), anotacion.getFecha());
        assertEquals(primera.getDescripcion(), anotacion.getDescripcion());
        assertEquals(primera.getDiagnostico(), anotacion.getDiagnostico());
        assertEquals(primera.getObservaciones(), anotacion.getObservaciones());
        assertEquals(List.of(formula, otraFormula), anotacion.getFormulasMedicas());
        assertSame(formula.getMedicamento(), anotacion.getFormulasMedicas().getFirst().getMedicamento());
        assertEquals(List.of(formulaSegunda), resultado.getFirst().getAnotaciones().get(1).getFormulasMedicas());
        assertEquals(List.of(formula, otraFormula), resultado.get(1).getAnotaciones().getFirst().getFormulasMedicas());
        verify(formulaMedicaRepository).findByCita_IdInOrderByFechaCreacionRegistroDesc(List.of(10L, 20L));
        verifyNoMoreInteractions(formulaMedicaRepository);
    }

    @Test
    void devuelveListaVaciaCuandoLaCitaNoTieneFormulas() {
        HistoriaMedica historia = new HistoriaMedica();
        when(historiaMedicaRepository.findAll()).thenReturn(List.of(historia));
        when(anotacionHistoriaRepository.findByHistoria(historia)).thenReturn(List.of(anotacion(historia, 10L)));
        when(formulaMedicaRepository.findByCita_IdInOrderByFechaCreacionRegistroDesc(List.of(10L)))
                .thenReturn(List.of());

        assertTrue(historiaService.getAllHistorias().getFirst().getAnotaciones().getFirst()
                .getFormulasMedicas().isEmpty());
    }

    @Test
    void conservaHistoriasSinAnotacionesSinConsultarFormulas() {
        HistoriaMedica historia = new HistoriaMedica();
        when(historiaMedicaRepository.findAll()).thenReturn(List.of(historia));
        when(anotacionHistoriaRepository.findByHistoria(historia)).thenReturn(List.of());

        List<HistoriaMedicaRs> resultado = historiaService.getAllHistorias();

        assertEquals(1, resultado.size());
        assertTrue(resultado.getFirst().getAnotaciones().isEmpty());
        verifyNoInteractions(formulaMedicaRepository);
    }

    @Test
    void devuelveListaVaciaSiNoHayHistorias() {
        when(historiaMedicaRepository.findAll()).thenReturn(List.of());

        assertTrue(historiaService.getAllHistorias().isEmpty());
        verifyNoInteractions(anotacionHistoriaRepository, formulaMedicaRepository);
    }

    private AnotacionHistoria anotacion(HistoriaMedica historia, Long citaId) {
        Cita cita = new Cita();
        cita.setId(citaId);
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setId(citaId);
        anotacion.setHistoria(historia);
        anotacion.setCita(cita);
        anotacion.setMedico(new Medico());
        anotacion.setFecha(LocalDateTime.now());
        anotacion.setDescripcion("Consulta");
        anotacion.setDiagnostico("Dermatitis");
        anotacion.setObservaciones("Control en una semana");
        return anotacion;
    }

    private FormulaMedica formula(Cita cita) {
        FormulaMedica formula = new FormulaMedica();
        formula.setCita(cita);
        formula.setMedicamento(new Medicamento());
        formula.setDosis("1 tableta");
        formula.setFrecuencia("Cada 12 horas");
        formula.setDuracion("5 dias");
        formula.setIndicaciones("Con alimento");
        return formula;
    }
}
