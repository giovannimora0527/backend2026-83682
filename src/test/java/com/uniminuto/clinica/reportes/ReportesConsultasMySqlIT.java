package com.uniminuto.clinica.reportes;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="CLINICA_REPORTES_QA",matches="docker-disposable")
class ReportesConsultasMySqlIT {
    @Test void consultaLosCatorceReportesYValidaSusColumnasEnMySql() {
        var source=new DriverManagerDataSource(
                "jdbc:mysql://127.0.0.1:19508/clinica?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota",
                "clinica_qa",System.getenv("REPORTES_QA_MYSQL"));
        var simple=new JdbcTemplate(source);
        assertEquals("docker-disposable",simple.queryForObject("SELECT entorno FROM inventario_qa_guard",String.class));
        var jdbc=new NamedParameterJdbcTemplate(simple);
        var hoy=LocalDate.of(2026,11,19);
        for(var tipo:ReporteDatos.Tipo.values()) {
            var p=new MapSqlParameterSource().addValue("inicio",tipo==ReporteDatos.Tipo.LOTES?null:hoy.minusDays(5).atStartOfDay())
                .addValue("fin",hoy.plusDays(30).atStartOfDay()).addValue("hoy",hoy).addValue("proximo",hoy.plusDays(30));
            for(String campo:new String[]{"medico","especialidad","medicamento","lote","mascota","propietario","estado"}) {
                p.addValue(campo,null);
            }
            var consulta=ConsultasReporte.consulta(tipo,"DIA");
            var filas=assertDoesNotThrow(()->jdbc.queryForList(consulta.sql()+" LIMIT 10001",p),tipo::name);
            assertFalse(filas.isEmpty(),tipo::name);
            for(var columna:consulta.columnas()) assertTrue(filas.getFirst().containsKey(columna.clave()),
                    ()->tipo.name()+" "+columna.clave());
        }
    }
}
