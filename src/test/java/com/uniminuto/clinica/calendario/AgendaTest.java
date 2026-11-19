package com.uniminuto.clinica.calendario;

import static com.uniminuto.clinica.calendario.AgendaDatos.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.service.AlcanceMedico;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AgendaTest {
    private final LocalDate lunes=LocalDate.of(2026,11,23);
    private final AgendaRepositorio datos=mock(AgendaRepositorio.class);
    private final AlcanceMedico alcance=mock(AlcanceMedico.class);
    private final AgendaServicio agenda=new AgendaServicio(datos,alcance);

    @Test void interpretaLaCitaHistoricaSinCambiarSusCampos() {
        var cita=new Cita();
        cita.setFechaHora(lunes.atTime(10,0));
        cita.setEstado(" PROGRAMADA ");
        assertEquals(lunes.atTime(10,30),cita.getFechaFin());
        assertNull(cita.fechaFinRegistrada());
        assertEquals("pendiente",cita.getEstado());
    }

    @Test void rechazaFranjasQueSeCruzanYAceptaFranjasContiguas() {
        var a=new Franja(1,LocalTime.of(8,0),LocalTime.of(10,0));
        var b=new Franja(1,LocalTime.of(9,30),LocalTime.of(12,0));
        var c=new Franja(1,LocalTime.of(10,0),LocalTime.of(12,0));
        assertEquals(400,assertThrows(ResponseStatusException.class,
                ()->AgendaServicio.validarFranjas(List.of(a,b))).getStatusCode().value());
        assertEquals(List.of(a,c),AgendaServicio.validarFranjas(List.of(c,a)));
    }

    @Test void disponibilidadDescuentaCitaYAusenciaIncluyendoUnBordeDeLaFranja() {
        when(datos.medicoActivo(2,false)).thenReturn(true);
        when(datos.horarios(2)).thenReturn(List.of(new Franja(1,LocalTime.of(8,0),LocalTime.of(12,0))));
        when(datos.ocupados(eq(2L),any())).thenReturn(List.of(intervalo(7,9)));
        when(datos.ausencias(eq(2L),any())).thenReturn(List.of(new Ausencia(5L,lunes.atTime(10,0),
                lunes.atTime(11,0),"Capacitación")));
        assertEquals(List.of(intervalo(9,10),intervalo(11,12)),agenda.disponibilidad(2,lunes).libres());
        verify(alcance).exigirMedico(2);
    }

    @Test void consultaYExcelTienenSoloEventosDelMedicoVinculado() throws Exception {
        when(alcance.medicoId()).thenReturn(null);
        assertEquals(403,assertThrows(ResponseStatusException.class,
                ()->agenda.eventos(lunes,lunes,null)).getStatusCode().value());
        when(alcance.medicoId()).thenReturn(7);
        var evento=new Evento(1,lunes.atTime(9,0),lunes.atTime(9,30),"confirmada",
                "Médico QA","Paciente QA","Propietario QA","=texto clínico");
        when(datos.eventos(eq(7),any(),eq("confirmada"))).thenReturn(List.of(evento));
        var eventos=agenda.eventos(lunes,lunes,"confirmada");
        byte[] bytes=new AgendaExcel().generar(eventos);
        try (var libro=new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var hoja=libro.getSheetAt(0);
            assertEquals(1,hoja.getLastRowNum());
            assertEquals("Finalización",hoja.getRow(0).getCell(2).getStringCellValue());
            assertEquals("Paciente QA",hoja.getRow(1).getCell(4).getStringCellValue());
            assertEquals(CellType.STRING,hoja.getRow(1).getCell(6).getCellType());
        }
    }

    private Intervalo intervalo(int inicio,int fin) {
        return new Intervalo(lunes.atTime(inicio,0),lunes.atTime(fin,0));
    }
}
