package com.uniminuto.clinica.atencion;

import static com.uniminuto.clinica.atencion.ClinicaDatos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AtencionFase4Test {
    private static final LocalDateTime FECHA=LocalDateTime.of(2026,11,18,10,0);
    private static final Paciente PACIENTE=new Paciente(1,2,3,4,"finalizada",FECHA,"Veterinaria Úrsula",
            "REG-QA","Mascota Ñ","Ana Pérez");

    @Test void validaTarifaYRedondeaCadaImpuestoPorLinea() {
        assertDoesNotThrow(()->TarifasServicio.validar(tarifa("100","19","2")));
        assertThrows(ResponseStatusException.class,()->TarifasServicio.validar(tarifa("-1","0","0")));
        assertThrows(ResponseStatusException.class,()->TarifasServicio.validar(tarifa("1.001","0","0")));
        assertThrows(ResponseStatusException.class,()->TarifasServicio.validar(tarifa("100","90","20")));
        var total=AtencionServicio.totales(atencion());
        assertEquals(0,new BigDecimal("2.16").compareTo(total.impuestos()));
        assertEquals(0,new BigDecimal("32.41").compareTo(total.total()));
    }

    @Test void pdfConservaLasLineasConfirmadasYSeparaEntregaDeEstimacion() throws Exception {
        var documentos=new DocumentosClinicos();
        var at=atencion();
        try(var pdf=Loader.loadPDF(documentos.formula(at.formulas().getFirst(),PACIENTE))) {
            String contenido=new PDFTextStripper().getText(pdf);
            assertTrue(contenido.contains("Clínica Veterinaria UNIMINUTO"));
            assertTrue(contenido.contains("Medicamento Á") && contenido.contains("Medicamento B"));
            assertTrue(contenido.contains("Vía") && contenido.contains("Mascota Ñ"));
        }
        try(var pdf=Loader.loadPDF(documentos.prefactura(at))) {
            String contenido=new PDFTextStripper().getText(pdf);
            assertTrue(contenido.contains("Medicamentos formulados"));
            assertTrue(contenido.contains("Medicamentos entregados"));
            assertTrue(contenido.contains("TOTAL ESTIMADO"));
            assertFalse(contenido.contains("Borrador excluido"));
        }
    }

    private Tarifa tarifa(String valor,String iva,String impuesto) {
        return new Tarifa(null,new BigDecimal(valor),new BigDecimal(iva),new BigDecimal(impuesto),FECHA.toLocalDate(),null,null,null);
    }

    private Atencion atencion() {
        var cab=new Cabecera(1,1,1,1,"General","Motivo","Síntomas","Diagnóstico",null,null,FECHA,FECHA,FECHA,1);
        var s=new LineaServicio(1,"Consulta",1,new BigDecimal("10.25"),new BigDecimal("19"),new BigDecimal("2"));
        var f=new Formula(1,1,"Mascota Ñ","CONFIRMADA",1,FECHA,FECHA,
                List.of(linea(1,"Medicamento Á",1),linea(2,"Medicamento B",1)));
        var borrador=new Formula(2,1,"Mascota Ñ","BORRADOR",1,FECHA,null,List.of(linea(2,"Borrador excluido",1000)));
        return new Atencion(PACIENTE,cab,List.of(),List.of(s),List.of(),List.of(f,borrador));
    }

    private Detalle linea(long id,String nombre,int cantidad) {
        return new Detalle(id,nombre,"Dosis","Frecuencia","Duración",cantidad,"Oral",null,BigDecimal.TEN);
    }
}
