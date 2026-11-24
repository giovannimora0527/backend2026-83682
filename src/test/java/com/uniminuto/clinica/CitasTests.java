package com.uniminuto.clinica;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.entity.enums.TipoServicio;
import com.uniminuto.clinica.repository.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Citas, agenda, calendario y reporte (R2, 5.3). */
class CitasTests extends PruebaIntegracion {

    @Autowired CitaRepository citas;
    @Autowired CitaHistorialEstadoRepository historial;
    @Autowired HorarioMedicoRepository horarios;
    @Autowired MedicoRepository medicos;
    @Autowired EspecializacionRepository especializaciones;
    @Autowired ServicioRepository servicios;
    @Autowired MascotaRepository mascotas;
    @Autowired RazaRepository razas;
    @Autowired ClienteRepository clientes;
    @Autowired ObjectMapper json;

    private final LocalDate lunes = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    private String secretaria;
    private Usuario usuarioMedico;
    private Medico medico;
    private Medico otroMedico;
    private Mascota luna;
    private Mascota max;

    @BeforeEach
    void preparar() {
        secretaria = token("SECRETARIA");
        usuarioMedico = usuario("MEDICO");
        medico = medico(usuarioMedico, "1");
        otroMedico = medico(usuario("MEDICO"), "2");
        luna = mascota("Luna", "10");
        max = mascota("Max", "20");
    }

    @Override
    protected void limpiarDatos() {
        historial.deleteAllInBatch();
        citas.deleteAllInBatch();
        servicios.deleteAllInBatch();
        horarios.deleteAllInBatch();
        medicos.deleteAllInBatch();
        especializaciones.deleteAllInBatch();
        mascotas.deleteAllInBatch();
        razas.deleteAllInBatch();
        clientes.deleteAllInBatch();
    }

    @Test
    void creaConDuracionDelServicioYDejaHistorial() throws Exception {
        Servicio cirugia = servicio(60);
        long id = id(crear(luna, medico, a(9, 0), cirugia.getId()).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fechaFin").value(lunes + "T10:00:00"))
                .andExpect(jsonPath("$.data.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.data.cliente.numeroDocumento").value("10")));
        mvc.perform(get("/cita/" + id + "/historial-estados").header("Authorization", secretaria))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estadoAnterior").isEmpty())
                .andExpect(jsonPath("$[0].estadoNuevo").value("PROGRAMADA"))
                .andExpect(jsonPath("$[0].usuario").isNotEmpty());
    }

    @Test
    void rechazaSolapamientoDelMedicoYDeLaMascota() throws Exception {
        crear(luna, medico, a(9, 0), null).andExpect(status().isOk());
        conflicto(crear(max, medico, a(9, 29), null), "El médico ya tiene");
        conflicto(crear(luna, otroMedico, a(8, 45), null), "La mascota ya tiene");
        crear(max, medico, a(9, 30), null).andExpect(status().isOk());
        crear(max, otroMedico, a(8, 30), null).andExpect(status().isOk());
    }

    @Test
    void rechazaFueraDeHorarioPasadoMedicoInactivoYMascotaInactiva() throws Exception {
        conflicto(crear(luna, medico, a(7, 30), null), "fuera del horario");
        conflicto(crear(luna, medico, a(11, 45), null), "fuera del horario");
        conflicto(crear(luna, medico, lunes.plusDays(5).atTime(9, 0), null), "fuera del horario");
        conflicto(crear(luna, medico, LocalDateTime.now().minusMinutes(5), null), "futuras");
        conflicto(crear(luna, medico, a(23, 45), null), "medianoche");
        medico.setEstado(false);
        medicos.save(medico);
        conflicto(crear(luna, medico, a(9, 0), null), "médico está inactivo");
        luna.setActivo(false);
        mascotas.save(luna);
        conflicto(crear(luna, otroMedico, a(9, 0), null), "mascota está inactiva");
        assertEquals(0, citas.count());
    }

    @Test
    void validaPropietarioServicioYDatos() throws Exception {
        enviar(post("/cita/crear"), secretaria, cuerpo(null, luna, medico, a(9, 0), ",\"clienteId\":"
                + max.getCliente().getId())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0].campo").value("clienteId"));
        Servicio inactivo = servicio(30);
        inactivo.setActivo(false);
        servicios.save(inactivo);
        conflicto(crear(luna, medico, a(9, 0), inactivo.getId()), "inactivos");
        enviar(post("/cita/crear"), secretaria, "{}").andExpect(status().isBadRequest());
        enviar(post("/cita/crear"), secretaria, cuerpo(null, luna, medico, a(9, 0), ",\"servicioId\":99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void editarNoChocaConsigoMismaPeroSiConOtras() throws Exception {
        long id = id(crear(luna, medico, a(9, 0), null));
        crear(max, medico, a(10, 0), null).andExpect(status().isOk());
        enviar(post("/cita/actualizar"), secretaria, cuerpo(id, luna, medico, a(9, 0), ""))
                .andExpect(status().isOk());
        enviar(post("/cita/actualizar"), secretaria, cuerpo(id, luna, medico, a(9, 10), ""))
                .andExpect(status().isOk());
        conflicto(enviar(post("/cita/actualizar"), secretaria, cuerpo(id, luna, medico, a(9, 45), "")), "El médico ya");
        // Editar sin cambiar fecha ni estado no agrega historial; moverla si.
        assertEquals(2, historial.findByCita_IdOrderByFechaAscIdAsc(id).size());
    }

    @Test
    void reprogramarRespetaReglasYGuardaLasDosFechas() throws Exception {
        long id = id(crear(luna, medico, a(9, 0), servicio(60).getId()));
        crear(max, medico, a(14, 0), null).andExpect(status().isOk());
        conflicto(reprogramar(id, a(13, 30)), "fuera del horario");
        conflicto(reprogramar(id, a(14, 15)), "El médico ya tiene");
        conflicto(reprogramar(id, LocalDateTime.now().minusDays(1)), "futuras");
        reprogramar(id, a(9, 30)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fechaInicio").value(lunes + "T09:30:00"))
                .andExpect(jsonPath("$.data.fechaFin").value(lunes + "T10:30:00"));
        mvc.perform(get("/cita/" + id + "/historial-estados").header("Authorization", secretaria))
                .andExpect(jsonPath("$[1].fechaInicioAnterior").value(lunes + "T09:00:00"))
                .andExpect(jsonPath("$[1].fechaInicioNueva").value(lunes + "T09:30:00"));
        estado(id, "CANCELADA", "No asiste").andExpect(status().isOk());
        conflicto(reprogramar(id, a(11, 0)), "Solo se editan");
        conflicto(enviar(post("/cita/actualizar"), secretaria, cuerpo(id, luna, medico, a(11, 0), "")), "Solo se editan");
        enviar(patch("/cita/" + id + "/reprogramar"), secretaria, "{}").andExpect(status().isBadRequest());
    }

    @Test
    void canceladaExigeMotivoLiberaElHorarioYConservaHistorial() throws Exception {
        long id = id(crear(luna, medico, a(9, 0), null));
        estado(id, "CANCELADA", "").andExpect(status().isBadRequest());
        estado(id, "CANCELADA", "Viaje").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.motivoCancelacion").value("Viaje"));
        crear(max, medico, a(9, 0), null).andExpect(status().isOk());
        mvc.perform(get("/cita/historial-mascota/" + luna.getMascotaId()).header("Authorization", secretaria))
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].estado").value("CANCELADA"));
        mvc.perform(get("/cita/" + id + "/historial-estados").header("Authorization", secretaria))
                .andExpect(jsonPath("$[1].motivo").value("Viaje"));
        assertEquals(2, citas.count());
    }

    @Test
    void transicionesDeEstado() throws Exception {
        long id = id(crear(luna, medico, a(9, 0), null));
        conflicto(estado(id, "EN_ATENCION", null), "solo se asigna al iniciar la atención");
        conflicto(estado(id, "ATENDIDA", null), "solo se asigna al finalizar la atención");
        estado(id, "CONFIRMADA", null).andExpect(status().isOk());
        conflicto(estado(id, "PROGRAMADA", null), "no puede pasar");
        estado(id, "OTRO", null).andExpect(status().isBadRequest());
        Cita cita = citas.findById(id).orElseThrow();
        cita.setEstado(EstadoCita.EN_ATENCION);
        citas.save(cita);
        conflicto(estado(id, "CANCELADA", "x"), "no puede pasar");
        conflicto(estado(id, "ATENDIDA", null), "solo se asigna");
        assertEquals(2, historial.count());
    }

    @Test
    void calendarioUsaElMedicoDelTokenEIncluyeCanceladas() throws Exception {
        long id = id(crear(luna, medico, a(9, 0), servicio(30).getId()));
        crear(max, otroMedico, a(9, 0), null).andExpect(status().isOk());
        estado(id, "CANCELADA", "x").andExpect(status().isOk());
        String rango = "?desde=" + lunes + "&hasta=" + lunes;
        mvc.perform(get("/cita/calendario" + rango + "&medicoId=" + otroMedico.getId())
                        .header("Authorization", token(usuarioMedico)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].title").value("Luna - Eva Paz"))
                .andExpect(jsonPath("$[0].start").value(lunes + "T09:00:00"))
                .andExpect(jsonPath("$[0].end").value(lunes + "T09:30:00"))
                .andExpect(jsonPath("$[0].color").value("#6c757d"))
                .andExpect(jsonPath("$[0].extendedProps.especie").value("Perro"))
                .andExpect(jsonPath("$[0].extendedProps.clienteDocumento").value("10"))
                .andExpect(jsonPath("$[0].extendedProps.servicioNombre").value("Servicio 30"));
        mvc.perform(get("/cita/calendario" + rango).header("Authorization", secretaria))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/cita/calendario" + rango + "&medicoId=" + otroMedico.getId())
                .header("Authorization", secretaria)).andExpect(jsonPath("$.length()").value(1));
        // Formatos que envia FullCalendar.
        mvc.perform(get("/cita/calendario").param("desde", lunes + "T00:00:00-05:00")
                .param("hasta", lunes.plusDays(1) + "T00:00:00-05:00").header("Authorization", secretaria))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/cita/calendario" + rango).header("Authorization", token("MEDICO")))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @CsvSource({"2026-02-01,2026-01-01", "2026-01-01,2027-06-01", "hoy,2026-01-01"})
    void rangosInvalidos(String desde, String hasta) throws Exception {
        for (String ruta : List.of("/cita/calendario", "/cita/resumen-estados", "/cita/reporte/excel")) {
            mvc.perform(get(ruta).param("desde", desde).param("hasta", hasta)
                    .header("Authorization", token("ADMINISTRADOR"))).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/cita/reporte/excel").header("Authorization", token("ADMINISTRADOR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void medicoNoOperaCitasAjenas() throws Exception {
        long id = id(crear(luna, otroMedico, a(9, 0), null));
        String token = token(usuarioMedico);
        mvc.perform(get("/cita/" + id + "/historial-estados").header("Authorization", token))
                .andExpect(status().isForbidden());
        enviar(patch("/cita/" + id + "/estado"), token, "{\"estado\":\"CONFIRMADA\"}").andExpect(status().isForbidden());
        enviar(patch("/cita/" + id + "/reprogramar"), token, "{\"fechaInicio\":\"" + a(10, 0) + ":00\"}")
                .andExpect(status().isForbidden());
        enviar(post("/cita/crear"), token, cuerpo(null, max, otroMedico, a(10, 0), "")).andExpect(status().isForbidden());
        enviar(post("/cita/crear"), token, cuerpo(null, max, medico, a(10, 0), "")).andExpect(status().isOk());
        mvc.perform(get("/cita/reporte/excel?desde=" + lunes + "&hasta=" + lunes).header("Authorization", secretaria))
                .andExpect(status().isForbidden());
    }

    @Test
    void resumenYExcelCoincidenYFiltranPorFecha() throws Exception {
        long id = id(crear(luna, medico, a(9, 0), null));
        crear(max, medico, a(10, 0), null).andExpect(status().isOk());
        crear(max, medico, a(9, 0).plusDays(1), null).andExpect(status().isOk());
        crear(luna, otroMedico, a(15, 0), null).andExpect(status().isOk());
        estado(id, "CANCELADA", "x").andExpect(status().isOk());
        String rango = "?desde=" + lunes + "&hasta=" + lunes;
        String token = token(usuarioMedico);
        mvc.perform(get("/cita/resumen-estados" + rango).header("Authorization", token))
                .andExpect(jsonPath("$.PROGRAMADA").value(1)).andExpect(jsonPath("$.CANCELADA").value(1))
                .andExpect(jsonPath("$.ATENDIDA").value(0));
        byte[] excel = mvc.perform(get("/cita/reporte/excel" + rango).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("citas_" + lunes)))
                .andReturn().getResponse().getContentAsByteArray();
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet hoja = libro.getSheet("Citas");
            assertEquals("Juan Rojas1", hoja.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Estado", hoja.getRow(5).getCell(9).getStringCellValue());
            assertNull(hoja.getRow(5).getCell(10));
            assertEquals("Luna", hoja.getRow(6).getCell(3).getStringCellValue());
            assertEquals("CANCELADA", hoja.getRow(6).getCell(9).getStringCellValue());
            assertNotEquals(hoja.getRow(6).getCell(0).getCellStyle(), hoja.getRow(7).getCell(0).getCellStyle());
            assertEquals("TOTAL", hoja.getRow(15).getCell(0).getStringCellValue());
            assertEquals(2, hoja.getRow(15).getCell(1).getNumericCellValue());
        }
        byte[] todos = mvc.perform(get("/cita/reporte/excel" + rango).header("Authorization", token("ADMINISTRADOR")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(todos))) {
            assertEquals("Médico", libro.getSheet("Citas").getRow(5).getCell(10).getStringCellValue());
        }
    }

    @Test
    void medicosDisponiblesRespetanHorarioYCitas() throws Exception {
        crear(luna, medico, a(9, 0), null).andExpect(status().isOk());
        disponibles(a(9, 0), a(9, 30)).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(otroMedico.getId()));
        disponibles(a(10, 0), a(10, 30)).andExpect(jsonPath("$.length()").value(2));
        disponibles(a(12, 30), a(13, 0)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/medico/listar-disponibles").param("fechaInicio", a(10, 0) + ":00")
                .param("fechaFin", a(10, 30) + ":00").header("Authorization", secretaria))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/medico/listar-disponibles").header("Authorization", secretaria))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creacionesSimultaneasSoloDejanUna() throws Exception {
        List<Callable<Integer>> tareas = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Mascota mascota = i % 2 == 0 ? luna : max;
            tareas.add(() -> crear(mascota, medico, a(9, 0), null).andReturn().getResponse().getStatus());
        }
        int creadas = 0;
        try (var hilos = Executors.newFixedThreadPool(6)) {
            for (Future<Integer> f : hilos.invokeAll(tareas)) {
                int estado = f.get();
                assertTrue(estado == 200 || estado == 409, "estado " + estado);
                creadas += estado == 200 ? 1 : 0;
            }
        }
        assertEquals(1, creadas);
        assertEquals(1, citas.count());
    }

    // ------------------------------------------------------------ apoyo

    private LocalDateTime a(int hora, int minuto) {
        return lunes.atTime(hora, minuto);
    }

    private ResultActions crear(Mascota mascota, Medico medico, LocalDateTime fecha, Long servicioId) throws Exception {
        return enviar(post("/cita/crear"), secretaria, cuerpo(null, mascota, medico, fecha,
                servicioId == null ? "" : ",\"servicioId\":" + servicioId));
    }

    private String cuerpo(Long id, Mascota mascota, Medico medico, LocalDateTime fecha, String extra) {
        return """
                {"id":%s,"mascotaId":%d,"medicoId":%d,"fechaInicio":"%s","motivo":"Control"%s}""".formatted(id,
                mascota.getMascotaId(), medico.getId(), fecha.withNano(0), extra);
    }

    private ResultActions reprogramar(long id, LocalDateTime fecha) throws Exception {
        return enviar(patch("/cita/" + id + "/reprogramar"), secretaria,
                "{\"fechaInicio\":\"" + fecha.withNano(0) + "\"}");
    }

    private ResultActions estado(long id, String estado, String motivo) throws Exception {
        return enviar(patch("/cita/" + id + "/estado"), secretaria, "{\"estado\":\"" + estado + "\",\"motivo\":"
                + (motivo == null ? "null" : "\"" + motivo + "\"") + "}");
    }

    private ResultActions disponibles(LocalDateTime inicio, LocalDateTime fin) throws Exception {
        return mvc.perform(get("/medico/listar-disponibles").param("inicio", inicio + ":00")
                .param("fin", fin + ":00").header("Authorization", secretaria)).andExpect(status().isOk());
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, String cuerpo) throws Exception {
        return mvc.perform(peticion.header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    private void conflicto(ResultActions r, String mensaje) throws Exception {
        r.andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString(mensaje)));
    }

    private long id(ResultActions r) throws Exception {
        return json.readTree(r.andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8)).get("data").get("id").asLong();
    }

    private Servicio servicio(int minutos) {
        Servicio servicio = new Servicio();
        servicio.setCodigo("S" + servicios.count());
        servicio.setNombre("Servicio " + minutos);
        servicio.setTipoServicio(TipoServicio.CONSULTA);
        servicio.setDuracionMinutos(minutos);
        return servicios.save(servicio);
    }

    private Medico medico(Usuario usuario, String documento) {
        Especializacion especializacion = new Especializacion();
        especializacion.setNombre("E" + documento);
        especializacion.setCodigoEspecializacion("C" + documento);
        Medico medico = new Medico();
        medico.setTipoDocumento("CC");
        medico.setNumeroDocumento(documento);
        medico.setNombres("Juan");
        medico.setApellidos("Rojas" + documento);
        medico.setRegistroProfesional("RP" + documento);
        medico.setEspecializacion(especializaciones.save(especializacion));
        medico.setEstado(true);
        medico.setUsuarioId(usuario.getId());
        medico = medicos.save(medico);
        for (int dia = 1; dia <= 5; dia++) {
            bloque(medico, dia, 8, 12);
            bloque(medico, dia, 14, 18);
        }
        return medico;
    }

    private void bloque(Medico medico, int dia, int desde, int hasta) {
        HorarioMedico horario = new HorarioMedico();
        horario.setMedico(medico);
        horario.setDiaSemana(dia);
        horario.setHoraInicio(LocalTime.of(desde, 0));
        horario.setHoraFin(LocalTime.of(hasta, 0));
        horarios.save(horario);
    }

    private Mascota mascota(String nombre, String documento) {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento(documento);
        cliente.setNombres("Eva");
        cliente.setApellidos("Paz");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setActivo(true);
        Raza raza = new Raza();
        raza.setNombre("Criollo");
        raza.setEspecie("Perro");
        raza.setFechaCreacion(LocalDateTime.now());
        Mascota mascota = new Mascota();
        mascota.setNombreMascota(nombre);
        mascota.setEdad(3);
        mascota.setCliente(clientes.save(cliente));
        mascota.setRaza(razas.save(raza));
        mascota.setActivo(true);
        return mascotas.save(mascota);
    }
}
