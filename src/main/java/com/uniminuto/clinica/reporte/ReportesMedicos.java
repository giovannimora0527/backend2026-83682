package com.uniminuto.clinica.reporte;

import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.util.RangoFechas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Configuration
public class ReportesMedicos {

    private static final String GRUPO = "Gestión médica";
    private static final String EN_RANGO = "c.fechaInicio >= :desde AND c.fechaInicio < :hasta ";
    /** Consultas: atenciones finalizadas por su fecha de inicio. */
    private static final String ATENCIONES = "a.finalizada = true AND a.fechaInicio >= :desde AND a.fechaInicio < :hasta ";

    private final Consulta consulta;

    public ReportesMedicos(Consulta consulta) {
        this.consulta = consulta;
    }

    @Bean
    GeneradorReporte citasPeriodo() {
        return GeneradorReporte.de("citas-periodo", "Citas por período", GRUPO,
                List.of(Filtro.DESDE, Filtro.HASTA, Filtro.AGRUPACION), f -> {
            String agrupacion = f.texto("agrupacion", "MES");
            String periodo = Consulta.periodo("c.fechaInicio", agrupacion);
            Map<String, Map<EstadoCita, Long>> periodos = new TreeMap<>();
            rango("SELECT " + periodo + ", c.estado, COUNT(c) FROM Cita c WHERE " + EN_RANGO + "GROUP BY " + periodo
                    + ", c.estado", f).forEach(r -> periodos.computeIfAbsent(Consulta.nombrePeriodo(r, agrupacion),
                    k -> new EnumMap<>(EstadoCita.class)).put((EstadoCita) r[3], (Long) r[4]));
            List<Columna> c = List.of(Columna.texto("periodo", "Período"), Columna.numero("programadas", "Pendientes"),
                    Columna.numero("confirmadas", "Confirmadas"), Columna.numero("enAtencion", "En atención"),
                    Columna.numero("atendidas", "Atendidas"), Columna.numero("canceladas", "Canceladas"),
                    Columna.numero("total", "Total"));
            return Tabla.de(c, periodos.entrySet().stream().map(e -> {
                Map<EstadoCita, Long> n = e.getValue();
                return Tabla.fila(c, e.getKey(), n.getOrDefault(EstadoCita.PROGRAMADA, 0L),
                        n.getOrDefault(EstadoCita.CONFIRMADA, 0L), n.getOrDefault(EstadoCita.EN_ATENCION, 0L),
                        n.getOrDefault(EstadoCita.ATENDIDA, 0L), n.getOrDefault(EstadoCita.CANCELADA, 0L),
                        n.values().stream().mapToLong(Long::longValue).sum());
            }).toList(), "programadas", "confirmadas", "enAtencion", "atendidas", "canceladas", "total");
        });
    }

    @Bean
    GeneradorReporte citasEstado() {
        List<String> estados = new ArrayList<>(List.of("TODAS"));
        Arrays.stream(EstadoCita.values()).map(Enum::name).forEach(estados::add);
        return GeneradorReporte.de("citas-estado", "Citas por estado", GRUPO,
                List.of(Filtro.DESDE, Filtro.HASTA, Filtro.opciones("estado", estados)), f -> {
            String estado = f.texto("estado", "TODAS");
            RangoFechas r = f.rango();
            List<Columna> c = List.of(Columna.fechaHora("inicio", "Inicio"), Columna.fechaHora("fin", "Fin"),
                    Columna.texto("mascota", "Mascota"), Columna.texto("cliente", "Propietario"),
                    Columna.texto("veterinario", "Veterinario"), Columna.texto("servicio", "Servicio"),
                    Columna.texto("motivo", "Motivo"), Columna.texto("estado", "Estado"), Columna.numero("cantidad", "Citas"));
            return Tabla.de(c, consulta.filas("SELECT c.fechaInicio, c.fechaFin, m.nombreMascota, "
                    + "CONCAT(p.nombres, ' ', p.apellidos), CONCAT(d.nombres, ' ', d.apellidos), s.nombre, c.motivo, c.estado "
                    + "FROM Cita c JOIN c.mascota m JOIN c.cliente p JOIN c.medico d LEFT JOIN c.servicio s WHERE " + EN_RANGO
                    + "AND (:estado IS NULL OR c.estado = :estado) ORDER BY c.fechaInicio, c.id", "desde", r.inicio(),
                    "hasta", r.finExclusivo(), "estado", "TODAS".equals(estado) ? null : EstadoCita.valueOf(estado)).stream()
                    .map(x -> Tabla.fila(c, x[0], x[1], x[2], x[3], x[4], x[5], x[6], x[7], 1)).toList(), "cantidad");
        });
    }

    @Bean
    GeneradorReporte consultasVeterinario() {
        return consultas("consultas-veterinario", "Consultas por veterinario", "Veterinario", "medicoId", "d.id",
                "CONCAT(d.nombres, ' ', d.apellidos)");
    }

    @Bean
    GeneradorReporte consultasEspecialidad() {
        return consultas("consultas-especialidad", "Consultas por especialidad", "Especialidad", "especializacionId", "e.id",
                "e.nombre");
    }

    @Bean
    GeneradorReporte mascotasAtendidas() {
        return GeneradorReporte.de("mascotas-atendidas", "Mascotas atendidas", GRUPO,
                List.of(Filtro.DESDE, Filtro.HASTA), f -> {
            List<Columna> c = List.of(Columna.texto("mascota", "Mascota"), Columna.texto("especie", "Especie"),
                    Columna.texto("raza", "Raza"), Columna.texto("cliente", "Propietario"),
                    Columna.numero("atenciones", "Atenciones"), Columna.fechaHora("ultima", "Última atención"),
                    Columna.numero("mascotas", "Mascotas"));
            return Tabla.de(c, rango("SELECT m.mascotaId, m.nombreMascota, r.especie, r.nombre, "
                    + "CONCAT(p.nombres, ' ', p.apellidos), COUNT(a), MAX(a.fechaInicio) FROM AtencionMedica a "
                    + "JOIN a.mascota m LEFT JOIN m.raza r JOIN m.cliente p WHERE " + ATENCIONES
                    + "GROUP BY m.mascotaId, m.nombreMascota, r.especie, r.nombre, p.nombres, p.apellidos "
                    + "ORDER BY m.nombreMascota", f).stream().map(x -> Tabla.fila(c, x[1], x[2], x[3], x[4], x[5], x[6], 1))
                    .toList(), "atenciones", "mascotas");
        });
    }

    private GeneradorReporte consultas(String codigo, String titulo, String columna, String filtro, String clave,
                                       String nombre) {
        return GeneradorReporte.de(codigo, titulo, GRUPO,
                List.of(Filtro.DESDE, Filtro.HASTA, Filtro.opcional(filtro)), f -> {
            RangoFechas r = f.rango();
            Long id = f.id(filtro);
            List<Columna> c = List.of(Columna.texto("nombre", columna), Columna.numero("consultas", "Consultas"),
                    Columna.numero("mascotas", "Mascotas distintas"), Columna.numero("canceladas", "Citas canceladas"));
            Map<Object, Object[]> grupos = new LinkedHashMap<>();
            consulta.filas("SELECT " + clave + ", MAX(" + nombre + "), COUNT(a), COUNT(DISTINCT a.mascota.mascotaId) "
                    + "FROM AtencionMedica a JOIN a.medico d JOIN d.especializacion e WHERE " + ATENCIONES
                    + "AND (:id IS NULL OR " + clave + " = :id) GROUP BY " + clave, "desde", r.inicio(), "hasta",
                    r.finExclusivo(), "id", id).forEach(x -> grupos.put(x[0], new Object[]{x[1], x[2], x[3], 0L}));
            consulta.filas("SELECT " + clave + ", MAX(" + nombre + "), COUNT(c) FROM Cita c JOIN c.medico d "
                    + "JOIN d.especializacion e WHERE " + EN_RANGO + "AND c.estado = "
                    + "com.uniminuto.clinica.entity.enums.EstadoCita.CANCELADA AND (:id IS NULL OR " + clave + " = :id) "
                    + "GROUP BY " + clave, "desde", r.inicio(), "hasta", r.finExclusivo(), "id", id).forEach(x ->
                    grupos.computeIfAbsent(x[0], k -> new Object[]{x[1], 0L, 0L, 0L})[3] = x[2]);
            return Tabla.de(c, grupos.values().stream().sorted(Comparator.comparing(g -> (String) g[0],
                    String.CASE_INSENSITIVE_ORDER)).map(g -> Tabla.fila(c, g)).toList(), "consultas", "canceladas");
        });
    }

    private List<Object[]> rango(String jpql, Filtros f) {
        RangoFechas r = f.rango();
        return consulta.filas(jpql, "desde", r.inicio(), "hasta", r.finExclusivo());
    }
}
