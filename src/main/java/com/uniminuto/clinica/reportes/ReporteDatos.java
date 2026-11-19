package com.uniminuto.clinica.reportes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

final class ReporteDatos {
    private ReporteDatos() {}

    enum Tipo {
        FINANCIERO, FACTURAS, PAGOS, POR_MEDICO, POR_ESPECIALIDAD, CONCEPTOS,
        EXISTENCIAS, LOTES, MOVIMIENTOS, DISPENSACIONES, CITAS, CONSULTAS, ATENCIONES, CITAS_OPERATIVAS
    }

    enum Formato { TEXTO, ENTERO, COP, FECHA, FECHAHORA }
    record Columna(String clave,String titulo,Formato formato,boolean sumar) {}
    record Filtro(LocalDate desde,LocalDate hasta,Long medico,Long especialidad,Long medicamento,
                  Long lote,Long mascota,Long propietario,String estado,String agrupacion) {}
    record Resultado(String titulo,LocalDateTime generado,Map<String,String> filtros,List<Columna> columnas,
                     Map<String,Object> totales,List<Map<String,Object>> filas,int registros,int pagina) {}
    record Consulta(String sql,List<Columna> columnas) {}

    static Columna texto(String k,String nombre) { return new Columna(k,nombre,Formato.TEXTO,false); }
    static Columna id(String k,String nombre) { return new Columna(k,nombre,Formato.ENTERO,false); }
    static Columna cantidad(String k,String nombre) { return new Columna(k,nombre,Formato.ENTERO,true); }
    static Columna dinero(String k,String nombre,boolean sumar) { return new Columna(k,nombre,Formato.COP,sumar); }
    static Columna fecha(String k,String nombre) { return new Columna(k,nombre,Formato.FECHA,false); }
    static Columna hora(String k,String nombre) { return new Columna(k,nombre,Formato.FECHAHORA,false); }
}
