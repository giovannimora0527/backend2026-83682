package com.uniminuto.clinica.inventario;

import static com.uniminuto.clinica.inventario.InventarioDatos.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

class InventarioTest {
    private static final LocalDate FECHA=LocalDate.of(2026,1,1);
    private final InventarioRepositorio repo=mock(InventarioRepositorio.class);
    private final InventarioServicio servicio=new InventarioServicio(repo);

    @Test void validaCodigoPrecioInvimaYFechas() {
        assertEquals("ABC00001",InventarioServicio.validar(producto("abc00001",BigDecimal.ZERO)).codigo());
        assertThrows(ResponseStatusException.class,()->InventarioServicio.validar(producto("INVALID",BigDecimal.ZERO)));
        assertThrows(ResponseStatusException.class,()->InventarioServicio.validar(producto("ab0000ß",BigDecimal.ZERO)));
        assertThrows(ResponseStatusException.class,()->InventarioServicio.validar(producto("ABC00001",BigDecimal.valueOf(-1))));
        var sinInvima=new Producto("ABC00001","Medicamento",null,null,TipoProducto.MEDICAMENTO,null,
                BigDecimal.ZERO,BigDecimal.ZERO,0,FECHA,FECHA,null,null,true);
        assertThrows(ResponseStatusException.class,()->InventarioServicio.validar(sinInvima));
        assertThrows(ResponseStatusException.class,()->InventarioServicio.validarLote(
                new RegistrarLote(" L1 ",BigDecimal.ZERO,FECHA,FECHA.minusDays(1),1,null)));
    }

    @Test void creaEditaYRechazaDuplicado() {
        Producto p=producto("ABC00001",BigDecimal.ONE);
        var ficha=new Ficha(9L,p,0,0,false,false,null,LocalDateTime.now(),null);
        when(repo.crear(eq(p),any())).thenReturn(9L);
        when(repo.catalogo(any(),eq(9L))).thenReturn(List.of(ficha));
        assertEquals(ficha,servicio.guardar(null,p));
        assertEquals(ficha,servicio.guardar(9L,p));
        verify(repo).bloquearProducto(9L);
        verify(repo).editar(eq(9L),eq(p),any());
        when(repo.crear(eq(p),any())).thenThrow(new DuplicateKeyException("duplicado"));
        assertEquals(409,assertThrows(ResponseStatusException.class,()->servicio.guardar(null,p)).getStatusCode().value());
    }

    @Test void registraIngresoInicialYControlaSaldoYVencimiento() {
        when(repo.bloquearProducto(1)).thenReturn(true);
        var nuevo=new RegistrarLote("LOTE1",BigDecimal.ONE,FECHA,null,5,null);
        when(repo.crearLote(1,nuevo)).thenReturn(2L);
        assertEquals(2L,servicio.registrarLote(1,nuevo,3));
        verify(repo).movimiento(eq(2L),eq(3L),argThat(m->m.tipo()==TipoMovimiento.ENTRADA && m.cantidad()==5),eq(5),any());

        var vigente=new Lote(2,1,"LOTE1",BigDecimal.ONE,FECHA,null,5,false,false,true);
        var vencido=new Lote(4,1,"LOTE2",BigDecimal.ONE,FECHA,FECHA,5,true,false,false);
        when(repo.bloquearLote(eq(1L),eq(2L),any(),eq(true))).thenReturn(vigente);
        when(repo.bloquearLote(eq(1L),eq(4L),any(),eq(true))).thenReturn(vencido);
        when(repo.lotes(eq(1L),any())).thenReturn(List.of(vencido,vigente));
        assertEquals(List.of(vigente),servicio.lotes(1,true));
        assertEquals(2,servicio.registrarMovimiento(1,2,new RegistrarMovimiento(TipoMovimiento.SALIDA,3,"Consumo",null),3));
        assertThrows(ResponseStatusException.class,()->servicio.registrarMovimiento(1,2,
                new RegistrarMovimiento(TipoMovimiento.SALIDA,6,"Consumo",null),3));
        assertThrows(ResponseStatusException.class,()->servicio.registrarMovimiento(1,4,
                new RegistrarMovimiento(TipoMovimiento.SALIDA,1,"Consumo",null),3));
        assertEquals(4,servicio.registrarMovimiento(1,4,new RegistrarMovimiento(TipoMovimiento.AJUSTE,-1,"Baja",null),3));
    }

    @Test void leeCsvYExcelDe56FilasYRechazaFormatoIncorrecto() throws Exception {
        var lector=new ArchivoInventario();
        var csv=new StringBuilder(String.join(",",ArchivoInventario.COLUMNAS)).append(System.lineSeparator());
        for (int i=1;i<=56;i++) csv.append(String.join(",",fila(i))).append(System.lineSeparator());
        assertEquals(56,lector.leer(archivo("inventario.csv",csv.toString())).stream().filter(f->f.errores().isEmpty()).count());
        try (var xlsx=new XSSFWorkbook();var salida=new ByteArrayOutputStream()) {
            var hoja=xlsx.createSheet();
            var encabezado=hoja.createRow(0);
            for (int columna=0;columna<ArchivoInventario.COLUMNAS.size();columna++) {
                encabezado.createCell(columna).setCellValue(ArchivoInventario.COLUMNAS.get(columna));
            }
            for (int i=1;i<=56;i++) {
                var valores=fila(i);
                var fila=hoja.createRow(i);
                for (int j=0;j<valores.size();j++) fila.createCell(j).setCellValue(valores.get(j));
            }
            hoja.getRow(56).getCell(6).setCellFormula("1+1");
            xlsx.write(salida);
            var filas=lector.leer(new MockMultipartFile("archivo","inventario.xlsx",null,salida.toByteArray()));
            assertEquals(56,filas.size());
            assertEquals(55,filas.stream().filter(f->f.errores().isEmpty()).count());
            assertFalse(filas.getLast().errores().isEmpty());
        }
        assertThrows(ResponseStatusException.class,()->lector.leer(archivo("corrupto.xlsx","sin contenido Excel")));
        assertThrows(ResponseStatusException.class,()->lector.leer(archivo("incorrecto.csv","nombre,precio")));
        assertThrows(ResponseStatusException.class,()->lector.leer(archivo("inventario.exe","contenido")));
    }

    @Test void marcaDuplicadosEnLaVistaPreviaYConfirmaSoloValidos() {
        var lector=mock(ArchivoInventario.class);
        var json=JsonMapper.builder().findAndAddModules().build();
        var p=producto("ABC00001",BigDecimal.ONE);
        var rows=List.of(new Fila(2,p,null,List.of()),new Fila(3,p,null,List.of()));
        var carga=new ImportacionInventario(repo,servicio,lector,json);
        var archivo=archivo("productos.csv","contenido");
        when(lector.leer(archivo)).thenReturn(rows);
        var fecha=LocalDateTime.now();
        when(repo.guardarCarga(eq(7L),eq("productos.csv"),anyString(),eq(1),any())).thenAnswer(call->{
            String filas=call.getArgument(2);
            when(repo.carga(4L,false)).thenReturn(new InventarioRepositorio.Carga(
                    new Resumen(4,7,"productos.csv",fecha,null,0,1),filas));
            when(repo.carga(4L,true)).thenReturn(new InventarioRepositorio.Carga(
                    new Resumen(4,7,"productos.csv",fecha,null,0,1),filas));
            return 4L;
        });
        assertEquals(1,carga.previsualizar(archivo,7).resumen().rechazados());
        assertEquals(403,assertThrows(ResponseStatusException.class,()->carga.confirmar(4,8)).getStatusCode().value());
        carga.confirmar(4,7);
        verify(repo).crear(eq(p),any());
        verify(repo).confirmarCarga(eq(4L),anyString(),eq(1),eq(1),any());
    }

    private Producto producto(String codigo,BigDecimal precio) {
        return new Producto(codigo,"Producto",null,null,TipoProducto.UTENSILIO,null,
                precio,BigDecimal.ONE,0,FECHA,FECHA,null,null,true);
    }

    private List<String> fila(int i) {
        return List.of("VT%06d".formatted(i),"Producto "+i,"","","UTENSILIO","","1.25","2","1",
                FECHA.toString(),FECHA.toString(),"","","true","LOTE1","5");
    }

    private MockMultipartFile archivo(String nombre,String contenido) {
        return new MockMultipartFile("archivo",nombre,null,contenido.getBytes(StandardCharsets.UTF_8));
    }
}
