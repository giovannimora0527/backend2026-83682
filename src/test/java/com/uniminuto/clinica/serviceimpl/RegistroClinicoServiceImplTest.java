package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroClinicoServiceImplTest {
    @Mock AnotacionRepository anotaciones;
    @Mock HistoriaClinicaRepository historias;
    @Mock VeterinarioRepository veterinarios;
    @Mock FormulaMedicaRepository formulas;
    @Mock MascotaRepository mascotas;
    @InjectMocks AnotacionServiceImpl anotacionService;
    @InjectMocks FormulaMedicaServiceImpl formulaService;

    @Test
    void rechazaAnotacionConHistoriaInexistente() {
        when(historias.findById(9L)).thenReturn(Optional.empty());
        BadRequestException error = assertThrows(BadRequestException.class, () ->
                anotacionService.crear(new AnotacionRequest(9L, 3L, LocalDateTime.now(), "Evolución", null, null)));
        assertEquals(404, error.getStatus().value());
        verify(anotaciones, never()).save(any());
    }

    @Test
    void rechazaFormulaConDosisNoPositivaAntesDeConsultarLaBase() {
        FormulaRequest request = new FormulaRequest(2, null, 3L, LocalDate.now(), "Medicamento", BigDecimal.ZERO, "Cada 8 horas", 3, null);
        assertThrows(BadRequestException.class, () -> formulaService.crear(request));
        verifyNoInteractions(mascotas, historias, veterinarios, formulas);
    }

    @Test
    void rechazaFormulaEmitidaAntesDeLaConsulta() {
        Cliente cliente = new Cliente(); cliente.setClienteId(1L);
        Mascota mascota = new Mascota(); mascota.setMascotaId(2); mascota.setNombreMascota("Luna"); mascota.setCliente(cliente);
        Veterinario vet = new Veterinario(); vet.setVeterinarioId(3L);
        HistoriaClinica historia = new HistoriaClinica(); historia.setHistoriaClinicaId(5L); historia.setMascota(mascota); historia.setVeterinario(vet);
        historia.setFechaHora(LocalDateTime.now());
        when(veterinarios.findById(3L)).thenReturn(Optional.of(vet));
        when(historias.findById(5L)).thenReturn(Optional.of(historia));
        FormulaRequest request = new FormulaRequest(2, 5L, 3L, LocalDate.now().minusDays(1), "Medicamento", BigDecimal.ONE, "Cada 8 horas", 3, null);

        assertThrows(BadRequestException.class, () -> formulaService.crear(request));
        verify(formulas, never()).save(any());
    }
}
