package com.uniminuto.clinica.inventario;

import static com.uniminuto.clinica.inventario.InventarioDatos.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@EnabledIfEnvironmentVariable(named="CLINICA_INVENTARIO_IT", matches="docker-disposable")
class InventarioMySqlIT {
    @Autowired InventarioServicio inventario;
    @Autowired JdbcTemplate jdbc;
    private long usuario;

    @BeforeEach void verificarBaseDesechable() {
        assertEquals("clinica",jdbc.queryForObject("SELECT DATABASE()",String.class));
        assertEquals("docker-disposable",jdbc.queryForObject("SELECT entorno FROM inventario_qa_guard",String.class));
        usuario=jdbc.queryForObject("SELECT MIN(id) FROM usuario WHERE activo=1",Long.class);
    }

    @Test void dosSalidasConcurrentesNoPuedenConsumirElMismoSaldo() throws Exception {
        var hoy=LocalDate.now();
        String codigo=UUID.randomUUID().toString().replace("-","").substring(0,8);
        var producto=inventario.guardar(null,new Producto(codigo,"QA "+codigo,null,null,TipoProducto.UTENSILIO,
                null,BigDecimal.ONE,BigDecimal.valueOf(2),1,hoy,hoy,null,null,true));
        long id=producto.id();
        long lote=inventario.registrarLote(id,new RegistrarLote("QA",BigDecimal.ONE,hoy,null,10,null),usuario);
        inventario.registrarLote(id,new RegistrarLote("QA-2",BigDecimal.valueOf(2),hoy,null,4,null),usuario);
        assertEquals(14,inventario.catalogo().stream().filter(f->f.id()==id).findFirst().orElseThrow().existencia());
        var listos=new CountDownLatch(2);
        var iniciar=new CountDownLatch(1);
        try (var hilos=Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Integer> retirar=()->{
                listos.countDown();
                if (!iniciar.await(10,TimeUnit.SECONDS)) throw new IllegalStateException("No inició la carrera.");
                try {
                    return inventario.registrarMovimiento(id,lote,
                            new RegistrarMovimiento(TipoMovimiento.SALIDA,7,"Prueba concurrente",null),usuario);
                } catch (ResponseStatusException error) {
                    return -error.getStatusCode().value();
                }
            };
            var uno=hilos.submit(retirar);
            var dos=hilos.submit(retirar);
            assertTrue(listos.await(10,TimeUnit.SECONDS));
            iniciar.countDown();
            var resultados=new java.util.ArrayList<>(List.of(uno.get(20,TimeUnit.SECONDS),dos.get(20,TimeUnit.SECONDS)));
            resultados.sort(Integer::compareTo);
            assertEquals(List.of(-409,3),resultados);
        }
        assertEquals(7,inventario.catalogo().stream().filter(f->f.id()==id).findFirst().orElseThrow().existencia());
        assertEquals(3,inventario.movimientos(id).size());
    }
}
