package com.uniminuto.clinica.reportes;

import static com.uniminuto.clinica.calendario.AgendaServicio.*;
import static com.uniminuto.clinica.reportes.ReporteDatos.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.uniminuto.clinica.service.IdentidadService.Rol;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
class ReporteServicio {
    static final int PAGINA=50;
    private static final int MAX_FILAS=10000;
    private static final Set<Tipo> MEDICOS=Set.of(Tipo.CITAS,Tipo.CONSULTAS,Tipo.ATENCIONES,Tipo.CITAS_OPERATIVAS);
    private static final Set<Tipo> FINANZAS=Set.of(Tipo.FINANCIERO,Tipo.FACTURAS,Tipo.PAGOS,Tipo.POR_MEDICO,Tipo.POR_ESPECIALIDAD,Tipo.CONCEPTOS);
    private static final Set<Tipo> STOCK=Set.of(Tipo.EXISTENCIAS,Tipo.LOTES,Tipo.MOVIMIENTOS,Tipo.DISPENSACIONES);
    private static final List<String> ESTADOS_CITA=List.of("pendiente","confirmada","cancelada","atendida","finalizada");
    private final NamedParameterJdbcTemplate jdbc;

    ReporteServicio(NamedParameterJdbcTemplate jdbc) {
        this.jdbc=jdbc;
    }

    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    Resultado consultar(Tipo tipo,Filtro f,int pagina,boolean exportar) {
        if(!permite(identidad().rol(),tipo)) throw prohibido("Este reporte no está autorizado para tu perfil.");
        if(pagina<0 || pagina>MAX_FILAS/PAGINA) throw invalido("Página inválida.");
        validar(tipo,f);
        String grupo=f.agrupacion()==null?"DIA":f.agrupacion();
        LocalDateTime generado=ahora();
        boolean historico=tipo==Tipo.EXISTENCIAS && f.desde()!=null;
        if(historico && f.hasta().isAfter(generado.toLocalDate())) {
            throw invalido("La fecha de cierre no puede ser futura.");
        }
        var parametros=parametros(f,generado.toLocalDate());
        if(historico) {
            var lotes=jdbc.queryForList(ExistenciasHistoricas.LOTE_SIN_CONCILIAR,parametros,Long.class);
            if(!lotes.isEmpty()) {
                throw conflicto("No se pudo conciliar el historial del lote " + lotes.getFirst() + ".");
            }
        }
        var consulta=historico?ExistenciasHistoricas.consulta():ConsultasReporte.consulta(tipo,grupo);
        var filas=jdbc.queryForList(consulta.sql()+" LIMIT "+(MAX_FILAS+1),parametros);
        if(filas.size()>MAX_FILAS) throw invalido("El resultado supera 10.000 filas. Acota el período o agrega filtros.");
        var totales=totales(tipo,consulta.columnas(),filas,parametros);
        int comienzo=Math.min(pagina*PAGINA,filas.size());
        var seleccion=exportar?filas:filas.subList(comienzo,Math.min(comienzo+PAGINA,filas.size()));
        return new Resultado(historico?"Existencias históricas por lote":titulo(tipo),generado,etiquetas(tipo,f,grupo),consulta.columnas(),totales,
                seleccion.stream().map(ReporteServicio::normalizar).toList(),filas.size(),pagina);
    }

    static boolean permite(Rol rol,Tipo tipo) {
        return rol==Rol.ADMIN || rol==Rol.RECEPCION && tipo==Tipo.CITAS_OPERATIVAS;
    }

    private static void validar(Tipo tipo,Filtro f) {
        boolean fechas=f.desde()!=null && f.hasta()!=null;
        if(!fechas && (tipo!=Tipo.LOTES && tipo!=Tipo.EXISTENCIAS || f.desde()!=null || f.hasta()!=null)) {
            throw invalido("Indica las dos fechas del período.");
        }
        if(fechas && (f.hasta().isBefore(f.desde()) || f.desde().getYear()<1000 || f.hasta().getYear()>9998)) {
            throw invalido("Revisa el rango de fechas.");
        }
        if(tipo==Tipo.EXISTENCIAS && fechas && f.estado()!=null) {
            throw invalido("El filtro de estado solo se aplica a existencias actuales.");
        }
        if(f.agrupacion()!=null && (tipo!=Tipo.FINANCIERO || !Set.of("DIA","MES","ANIO").contains(f.agrupacion()))) {
            throw invalido("Agrupación inválida para el reporte.");
        }
        if(f.estado()!=null && !estados(tipo).contains(f.estado())) throw invalido("Estado inválido para el reporte.");
        for(Long id:new Long[]{f.medico(),f.especialidad(),f.medicamento(),f.lote(),f.mascota(),f.propietario()}) {
            if(id!=null && id<1) throw invalido("Los identificadores deben ser positivos.");
        }
        if((f.medico()!=null || f.especialidad()!=null) && !MEDICOS.contains(tipo) && !FINANZAS.contains(tipo)
                || f.medicamento()!=null && !STOCK.contains(tipo)
                || f.lote()!=null && (!STOCK.contains(tipo) || tipo==Tipo.EXISTENCIAS)
                || (f.mascota()!=null || f.propietario()!=null) && tipo!=Tipo.CITAS && tipo!=Tipo.CONSULTAS && tipo!=Tipo.ATENCIONES) {
            throw invalido("Hay filtros que no corresponden al tipo de reporte.");
        }
    }

    private static List<String> estados(Tipo tipo) {
        return switch(tipo) {
            case CITAS,CONSULTAS,ATENCIONES,CITAS_OPERATIVAS->ESTADOS_CITA;
            case FACTURAS->List.of("PENDIENTE_PAGO","PAGO_PARCIAL","PAGADA","ANULADA");
            case EXISTENCIAS->List.of("agotado","minimo","vencido","proximo");
            case MOVIMIENTOS->List.of("ENTRADA","SALIDA","AJUSTE");
            default->List.of();
        };
    }

    private Map<String,Object> totales(Tipo tipo,List<Columna> columnas,List<Map<String,Object>> filas,MapSqlParameterSource parametros) {
        var resumen=new LinkedHashMap<String,Object>();
        resumen.put("registros",filas.size());
        for(var c:columnas) if(c.sumar()) {
            BigDecimal suma=filas.stream().map(f->f.get(c.clave())).filter(Number.class::isInstance)
                    .map(n->new BigDecimal(n.toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
            resumen.put(c.clave(),suma.setScale(c.formato()==Formato.COP?2:0));
        }
        if(tipo==Tipo.ATENCIONES) resumen.put("mascotas_distintas",
                jdbc.queryForObject(ConsultasReporte.mascotasFinalizadas(),parametros,Long.class));
        if(tipo==Tipo.CITAS) {
            resumen.put("mascotas_atendidas",filas.stream().filter(f->((Number)f.get("atendidas")).intValue()==1)
                    .map(f->f.get("mascota_id")).distinct().count());
        }
        if(tipo==Tipo.CITAS || tipo==Tipo.CITAS_OPERATIVAS) {
            for(String estado:ESTADOS_CITA) {
                long cantidad=filas.stream().filter(f->estado.equals(f.get("estado")))
                        .mapToLong(f->tipo==Tipo.CITAS?1:((Number)f.get("citas")).longValue()).sum();
                resumen.put(estado,cantidad);
            }
        }
        return resumen;
    }

    private static MapSqlParameterSource parametros(Filtro f,LocalDate hoy) {
        return new MapSqlParameterSource().addValue("inicio",f.desde()==null?null:f.desde().atStartOfDay())
                .addValue("fin",f.hasta()==null?null:f.hasta().plusDays(1).atStartOfDay()).addValue("hoy",hoy)
                .addValue("proximo",hoy.plusDays(30)).addValue("medico",f.medico()).addValue("especialidad",f.especialidad())
                .addValue("medicamento",f.medicamento()).addValue("lote",f.lote()).addValue("mascota",f.mascota())
                .addValue("propietario",f.propietario()).addValue("estado",f.estado());
    }

    private static Map<String,String> etiquetas(Tipo tipo,Filtro f,String grupo) {
        var salida=new LinkedHashMap<String,String>();
        salida.put("Zona horaria","America/Bogota");
        if(f.desde()!=null) {
            salida.put(tipo==Tipo.LOTES?"Vencimiento desde":"Desde",f.desde().toString());
            salida.put(tipo==Tipo.LOTES?"Vencimiento hasta":"Hasta",f.hasta().toString());
        }
        if(tipo==Tipo.FINANCIERO) salida.put("Agrupación",grupo);
        if(tipo==Tipo.EXISTENCIAS && f.desde()!=null) {
            salida.put("Corte","Apertura antes del primer día y cierre inclusivo en Bogotá");
            salida.put("Cobertura","Lotes con movimientos registrados hasta el cierre");
            salida.put("Valoración","Saldo físico al costo registrado del lote");
        } else if(tipo==Tipo.EXISTENCIAS || tipo==Tipo.LOTES) {
            salida.put("Valoración","Saldo actual al momento de generar");
        }
        var valores=new Object[]{f.medico(),f.especialidad(),f.medicamento(),f.lote(),f.mascota(),f.propietario(),f.estado()};
        var nombres=List.of("ID médico","ID especialidad","ID medicamento","ID lote","ID mascota","ID propietario","Estado");
        for(int i=0;i<valores.length;i++) if(valores[i]!=null) salida.put(nombres.get(i),valores[i].toString());
        return salida;
    }

    private static Map<String,Object> normalizar(Map<String,Object> fila) {
        var salida=new LinkedHashMap<String,Object>();
        fila.forEach((k,v)->salida.put(k,switch(v) {
            case java.sql.Date d->d.toLocalDate();
            case Timestamp t->t.toLocalDateTime();
            case null->null;
            default->v;
        }));
        return salida;
    }

    private static String titulo(Tipo tipo) {
        return switch(tipo) {
            case FINANCIERO->"Resumen financiero";
            case FACTURAS->"Facturas y saldos al cierre";
            case PAGOS->"Pagos recibidos";
            case POR_MEDICO->"Facturación por veterinario";
            case POR_ESPECIALIDAD->"Facturación por especialidad";
            case CONCEPTOS->"Facturación por servicio y medicamento";
            case EXISTENCIAS->"Existencias y valoración";
            case LOTES->"Inventario por lote";
            case MOVIMIENTOS->"Movimientos de inventario";
            case DISPENSACIONES->"Dispensaciones y reversiones";
            case CITAS->"Gestión médica de citas";
            case CONSULTAS->"Consultas atendidas por veterinario y especialidad";
            case ATENCIONES->"Atenciones finalizadas";
            case CITAS_OPERATIVAS->"Resumen operativo de citas";
        };
    }
}
