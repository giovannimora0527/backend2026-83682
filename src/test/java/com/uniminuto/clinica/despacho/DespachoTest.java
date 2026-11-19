package com.uniminuto.clinica.despacho;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.uniminuto.clinica.despacho.DespachoDatos.Seleccion;
import com.uniminuto.clinica.inventario.InventarioDatos.Lote;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class DespachoTest {
    @Test void usaLotesVigentesEnOrdenFefoYDejaSinFechaAlFinal() {
        var lotes=List.of(lote(1,20,"2025-10-01",false),lote(5,3,"2026-07-01",true),
                lote(2,7,"2026-08-01",true),lote(3,10,null,true));
        assertEquals(List.of(new Seleccion(9,2,3),new Seleccion(9,5,3)),DespachoServicio.asignar(9,6,lotes));
        assertEquals(List.of(new Seleccion(9,2,7),new Seleccion(9,3,1),new Seleccion(9,5,3)),
                DespachoServicio.asignar(9,11,lotes));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->DespachoServicio.asignar(9,21,lotes))
                .getStatusCode().value());
    }

    private Lote lote(long id,int cantidad,String vence,boolean disponible) {
        return new Lote(id,9,"L"+id,BigDecimal.ONE,LocalDate.of(2025,1,1),
                vence==null?null:LocalDate.parse(vence),cantidad,!disponible,false,disponible);
    }
}
