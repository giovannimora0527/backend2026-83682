package com.uniminuto.clinica.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="CLINICA_REPORTES_QA",matches="docker-disposable")
class FinanzasReporteMySqlIT {
    @Test
    void conciliaFacturasYPagosDelDiaEnMySql() {
        var ds=new DriverManagerDataSource(
                "jdbc:mysql://127.0.0.1:19508/clinica?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota",
                "clinica_qa",System.getenv("REPORTES_QA_MYSQL"));
        var sql=new JdbcTemplate(ds);
        assertEquals("docker-disposable",sql.queryForObject("SELECT entorno FROM inventario_qa_guard",String.class));
        var dia=sql.queryForObject("SELECT DATE(MAX(emitida)) FROM factura WHERE estado='PAGADA'",
                (fila,i)->fila.getObject(1,LocalDate.class));
        assertNotNull(dia, "La base QA debe contener al menos una factura pagada.");
        var repo=new FinanzasReporteRepositorio(new NamedParameterJdbcTemplate(sql));
        var resumen=repo.resumen(dia,dia);
        var facturas=sql.queryForList("SELECT estado,total,pagado,iva,otros_impuestos,descuento FROM factura WHERE DATE(emitida)=?",dia);
        var validas=facturas.stream().filter(f->!"ANULADA".equals(f.get("estado"))).toList();
        assertEquals(validas.size(),resumen.facturasValidas());
        assertEquals(facturas.size()-validas.size(),resumen.anuladas());
        assertEquals(validas.stream().filter(f->"PAGADA".equals(f.get("estado"))).count(),resumen.pagadas());
        assertEquals(validas.stream().filter(f->"PAGO_PARCIAL".equals(f.get("estado"))).count(),resumen.parciales());
        assertEquals(validas.stream().filter(f->"PENDIENTE_PAGO".equals(f.get("estado"))).count(),resumen.pendientes());
        comparar(sumar(validas,"total"),resumen.emitido());
        comparar(sumar(validas,"total").subtract(sumar(validas,"pagado")),resumen.saldoAlCierre());
        comparar(sumar(validas,"iva"),resumen.ivaRegistrado());
        comparar(sumar(validas,"otros_impuestos"),resumen.otrosImpuestosRegistrados());
        comparar(sumar(validas,"descuento"),resumen.descuentos());
        comparar(sumar(facturas,"total").subtract(sumar(validas,"total")),resumen.importeAnulado());
        var caja=sql.queryForObject("""
                SELECT COALESCE(SUM(p.importe),0)
                FROM factura_pago p JOIN factura f ON f.id=p.factura_id
                WHERE DATE(p.fecha)=? AND p.estado='REGISTRADO' AND f.estado<>'ANULADA'
                """, BigDecimal.class,dia);
        comparar(caja,resumen.recaudado());
        var vacio=repo.resumen(LocalDate.of(1900,1,1),LocalDate.of(1900,1,1));
        assertEquals(0,vacio.facturasValidas());
        comparar(BigDecimal.ZERO,vacio.emitido());
        comparar(BigDecimal.ZERO,vacio.recaudado());
        assertThrows(IllegalArgumentException.class,()->repo.resumen(dia,dia.minusDays(1)));
    }

    private static BigDecimal sumar(List<Map<String,Object>> filas,String columna) {
        return filas.stream().map(fila -> (BigDecimal) fila.get(columna)).reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    private static void comparar(BigDecimal esperado,BigDecimal actual) {
        assertEquals(0,esperado.compareTo(actual),()->"Importe obtenido: "+actual);
    }
}
