package com.uniminuto.clinica;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.entity.enums.EstadoCita;
import com.uniminuto.clinica.entity.enums.TipoServicio;
import com.uniminuto.clinica.models.FormulaRs;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Tarifas, atencion, formulas y prefactura (R3.1 a R3.3). */
class AtencionFormulasTests extends PruebaIntegracion {

    @Autowired ImpuestoRepository impuestos;
    @Autowired ServicioRepository servicios;
    @Autowired TarifaRepository tarifas;
    @Autowired TarifaHistorialRepository tarifaHistorial;
    @Autowired ConfiguracionClinicaRepository configuracion;
    @Autowired AtencionMedicaRepository atenciones;
    @Autowired AtencionServicioRepository atencionServicios;
    @Autowired AnotacionHistoriaRepository anotaciones;
    @Autowired HistoriaMedicaRepository historias;
    @Autowired FormulaMedicaRepository formulas;
    @Autowired FormulaMedicaDetalleRepository detalles;
    @Autowired FormulaHistorialEstadoRepository formulaHistorial;
    @Autowired DespachoRepository despachos;
    @Autowired MedicamentoRepository medicamentos;
    @Autowired LoteMedicamentoRepository lotes;
    @Autowired MovimientoInventarioRepository movimientos;
    @Autowired CitaRepository citas;
    @Autowired CitaHistorialEstadoRepository citaHistorial;
    @Autowired MedicoRepository medicos;
    @Autowired EspecializacionRepository especializaciones;
    @Autowired MascotaRepository mascotas;
    @Autowired RazaRepository razas;
    @Autowired ClienteRepository clientes;
    @Autowired ObjectMapper json;

    private String admin;
    private String medico;
    private Usuario usuarioMedico;
    private Medico doctor;
    private Mascota luna;
    private Impuesto iva0;
    private Impuesto iva19;
    private Servicio consulta;
    private Medicamento amoxicilina;
    private Medicamento jarabe;

    @BeforeEach
    void preparar() {
        admin = token("ADMINISTRADOR");
        usuarioMedico = usuario("MEDICO");
        medico = token(usuarioMedico);
        ConfiguracionClinica c = new ConfiguracionClinica();
        c.setNombre("Clínica Prueba");
        c.setNit("900");
        c.setPrefijoFactura("FV");
        c.setPrefijoFormula("FM");
        c.setPrefijoDespacho("DP");
        configuracion.save(c);
        iva0 = impuesto("IVA 0", 0);
        iva19 = impuesto("IVA 19", 19);
        doctor = medico(usuarioMedico, "1");
        luna = mascota();
        consulta = servicio("CONS", TipoServicio.CONSULTA, doctor.getEspecializacion());
        tarifa(consulta, 50000, iva19);
        amoxicilina = medicamento("AMX00001", "Amoxicilina", 1500, iva0, 30);
        jarabe = medicamento("JAR00001", "Jarabe", 10000, iva19, 0);
    }

    @Override
    protected void limpiarDatos() {
        formulaHistorial.deleteAllInBatch();
        despachos.deleteAllInBatch();
        detalles.deleteAllInBatch();
        formulas.deleteAllInBatch();
        atencionServicios.deleteAllInBatch();
        atenciones.deleteAllInBatch();
        anotaciones.deleteAllInBatch();
        historias.deleteAllInBatch();
        citaHistorial.deleteAllInBatch();
        citas.deleteAllInBatch();
        movimientos.deleteAllInBatch();
        lotes.deleteAllInBatch();
        medicamentos.deleteAllInBatch();
        tarifaHistorial.deleteAllInBatch();
        tarifas.deleteAllInBatch();
        servicios.deleteAllInBatch();
        impuestos.deleteAllInBatch();
        configuracion.deleteAllInBatch();
        medicos.deleteAllInBatch();
        especializaciones.deleteAllInBatch();
        mascotas.deleteAllInBatch();
        razas.deleteAllInBatch();
        clientes.deleteAllInBatch();
    }

    // ------------------------------------------------------------ catalogos

    @Test
    void cambiarTarifaCierraLaAnteriorYDejaLog() throws Exception {
        long id = tarifas.findByServicio_IdAndActivaTrue(consulta.getId()).orElseThrow().getId();
        enviar(put("/tarifa/" + id), admin, "{\"valor\":60000,\"impuestoId\":" + iva0.getId() + "}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalles[0].campo").value("motivo"));
        long nueva = leer(enviar(put("/tarifa/" + id), admin, "{\"valor\":60000,\"impuestoId\":" + iva0.getId()
                + ",\"motivo\":\"Ajuste anual\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valor").value(60000)).andExpect(jsonPath("$.data.porcentajeIva").value(0)))
                .get("data").get("id").asLong();
        enviar(put("/tarifa/" + id), admin, "{\"valor\":1,\"impuestoId\":" + iva0.getId() + ",\"motivo\":\"x\"}")
                .andExpect(status().isConflict());
        enviar(put("/tarifa/" + nueva), admin, "{\"valor\":60000.00,\"impuestoId\":" + iva0.getId()
                + ",\"motivo\":\"x\"}").andExpect(status().isConflict());
        consultar("/tarifa/listar?servicioId=" + consulta.getId(), admin).andExpect(jsonPath("$.length()").value(2));
        consultar("/tarifa/listar?soloVigentes=true", admin).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(nueva));
        consultar("/tarifa/" + nueva + "/historial", admin).andExpect(jsonPath("$[0].valorAnterior").value(50000))
                .andExpect(jsonPath("$[0].valorNuevo").value(60000)).andExpect(jsonPath("$[0].ivaAnterior").value(19))
                .andExpect(jsonPath("$[0].motivo").value("Ajuste anual")).andExpect(jsonPath("$[0].usuario").isNotEmpty());
        consultar("/tarifa/historial?desde=" + LocalDate.now() + "&hasta=" + LocalDate.now(), admin)
                .andExpect(jsonPath("$.length()").value(1));
        enviar(post("/tarifa/crear"), admin, "{\"servicioId\":" + consulta.getId() + ",\"valor\":1,\"impuestoId\":"
                + iva0.getId() + "}").andExpect(status().isConflict());
    }

    @Test
    void impuestosYServiciosValidan() throws Exception {
        enviar(post("/impuesto/crear"), admin, "{\"nombre\":\"iva 19\",\"porcentaje\":19}").andExpect(status().isConflict());
        enviar(post("/impuesto/crear"), admin, "{\"nombre\":\"X\",\"porcentaje\":101}").andExpect(status().isBadRequest());
        enviar(put("/impuesto/" + iva19.getId()), admin, "{\"nombre\":\"IVA 19\",\"porcentaje\":16}")
                .andExpect(status().isConflict());
        enviar(put("/impuesto/" + iva19.getId()), admin, "{\"nombre\":\"IVA 19\",\"porcentaje\":19,\"activo\":false}")
                .andExpect(status().isConflict());
        enviar(post("/servicio/crear"), admin, "{\"codigo\":\"cons\",\"nombre\":\"X\",\"tipoServicio\":\"OTRO\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detalles[0].campo").value("codigo"));
        enviar(post("/servicio/crear"), admin, "{\"codigo\":\"vac\",\"nombre\":\"Vacuna\",\"tipoServicio\":\"VACUNACION\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.codigo").value("VAC"))
                .andExpect(jsonPath("$.data.duracionMinutos").value(30))
                .andExpect(jsonPath("$.data.tarifaVigente").isEmpty());
        consultar("/servicio/listar?especializacionId=" + doctor.getEspecializacion().getId(), medico)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tarifaVigente.porcentajeIva").value(19));
        Especializacion e = doctor.getEspecializacion();
        e.setActivo(false);
        especializaciones.save(e);
        consultar("/servicio/listar?soloActivos=true", medico).andExpect(jsonPath("$.length()").value(1));
        consultar("/servicio/listar", medico).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void configuracionSeEdita() throws Exception {
        enviar(put("/configuracion-clinica"), admin, """
                {"nombre":"Nueva","nit":"1","prefijoFactura":"fx","topeDescuentoSecretaria":15}""")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.prefijoFactura").value("FX"))
                .andExpect(jsonPath("$.data.prefijoFormula").value("FM"));
        consultar("/configuracion-clinica", medico).andExpect(jsonPath("$.nombre").value("Nueva"));
        enviar(put("/configuracion-clinica"), admin, "{\"nombre\":\"N\",\"nit\":\"1\",\"prefijoFactura\":\"F-1\"}")
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({"MEDICO,POST,/tarifa/crear", "SECRETARIA,PUT,/tarifa/1", "SECRETARIA,POST,/servicio/crear",
            "MEDICO,PUT,/configuracion-clinica", "SECRETARIA,POST,/impuesto/crear", "SECRETARIA,GET,/atencion/cita/1",
            "ADMINISTRADOR,POST,/atencion/iniciar/1", "ADMINISTRADOR,PUT,/atencion/1",
            "ADMINISTRADOR,POST,/atencion/1/finalizar", "DESPACHO_MEDICO,POST,/formula-medica/crear",
            "DESPACHO_MEDICO,PUT,/formula-medica/1", "DESPACHO_MEDICO,POST,/formula-medica/1/confirmar",
            "DESPACHO_MEDICO,POST,/formula-medica/1/anular", "DESPACHO_MEDICO,GET,/formula-medica/listar",
            "DESPACHO_MEDICO,GET,/formula-medica/cita/1", "ADMINISTRADOR,POST,/formula-medica/crear",
            "SECRETARIA,GET,/formula-medica/1", "DESPACHO_MEDICO,GET,/atencion/1/prefactura/pdf"})
    void permisos(String rol, String metodo, String ruta) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(metodo), ruta).header("Authorization", token(rol))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------ atencion

    @Test
    void iniciarEsIdempotenteYCambiaElEstado() throws Exception {
        Cita cita = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        long id = iniciar(cita);
        assertEquals(id, iniciar(cita));
        assertEquals(1, atenciones.count());
        assertEquals(1, historias.count());
        assertEquals(EstadoCita.EN_ATENCION, citas.findById(cita.getId()).orElseThrow().getEstado());
        assertEquals(1, citaHistorial.count());
        Cita otra = cita(LocalDateTime.now(), EstadoCita.PROGRAMADA);
        iniciar(otra);
        assertEquals(1, historias.count(), "La mascota tiene una sola historia");
    }

    @Test
    void noSeIniciaEnOtraFechaNiDeOtroMedicoNiCancelada() throws Exception {
        enviarIniciar(cita(LocalDateTime.now().plusDays(1), EstadoCita.CONFIRMADA), medico).andExpect(status().isConflict());
        enviarIniciar(cita(LocalDateTime.now().minusDays(1), EstadoCita.CONFIRMADA), medico).andExpect(status().isConflict());
        enviarIniciar(cita(LocalDateTime.now(), EstadoCita.CANCELADA), medico).andExpect(status().isConflict());
        Usuario otro = usuario("MEDICO");
        medico(otro, "2");
        Cita cita = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        enviarIniciar(cita, token(otro)).andExpect(status().isForbidden());
        consultar("/atencion/cita/" + cita.getId(), token(otro), 403);
        if (LocalDateTime.now().getHour() < 22) {
            enviarIniciar(cita(LocalDateTime.now().plusMinutes(50), EstadoCita.CONFIRMADA), medico)
                    .andExpect(status().isConflict());
            enviarIniciar(cita(LocalDateTime.now().plusMinutes(20), EstadoCita.CONFIRMADA), medico)
                    .andExpect(status().isOk());
        }
    }

    @Test
    void finalizarExigeDatosCierraLaCitaYAnotaEnLaHistoria() throws Exception {
        Cita cita = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        long id = iniciar(cita);
        enviar(post("/atencion/" + id + "/finalizar"), medico, "{}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0].campo").value("sintomas"))
                .andExpect(jsonPath("$.detalles[1].campo").value("diagnostico"));
        guardar(id, ",\"servicios\":[{\"servicioId\":" + consulta.getId() + ",\"cantidad\":1},{\"servicioId\":"
                + consulta.getId() + ",\"cantidad\":1}]").andExpect(status().isConflict());
        guardar(id, "").andExpect(status().isOk());
        enviar(post("/atencion/" + id + "/finalizar"), medico, "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalizada").value(true))
                .andExpect(jsonPath("$.data.estadoCita").value("ATENDIDA"))
                .andExpect(jsonPath("$.data.servicios[0].servicioCodigo").value("CONS"));
        assertEquals(EstadoCita.ATENDIDA, citas.findById(cita.getId()).orElseThrow().getEstado());
        AnotacionHistoria anotacion = anotaciones.findAll().getFirst();
        assertEquals("Otitis", anotacion.getDiagnostico());
        assertTrue(anotacion.getObservaciones().contains("Recomendaciones: Reposo"));
        guardar(id, "").andExpect(status().isConflict());
        enviar(post("/atencion/" + id + "/finalizar"), medico, "{}").andExpect(status().isConflict());
        consultar("/cita/" + cita.getId() + "/historial-estados", medico).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void finalizarRechazaServiciosSinTarifaYFormulasEnBorrador() throws Exception {
        long id = iniciar(cita(LocalDateTime.now(), EstadoCita.CONFIRMADA));
        Servicio sinTarifa = servicio("NADA", TipoServicio.OTRO, null);
        guardar(id, ",\"servicios\":[{\"servicioId\":" + sinTarifa.getId() + ",\"cantidad\":2}]")
                .andExpect(status().isOk());
        conflicto(enviar(post("/atencion/" + id + "/finalizar"), medico, "{}"), "no tiene tarifa vigente");
        guardar(id, ",\"servicios\":[]").andExpect(status().isOk());
        long formula = crearFormula(atenciones.findById(id).orElseThrow().getCita().getId(), detalle(amoxicilina, 21));
        conflicto(enviar(post("/atencion/" + id + "/finalizar"), medico, "{}"), "borrador");
        enviar(post("/formula-medica/" + formula + "/anular"), medico, "{\"motivo\":\"Error\"}").andExpect(status().isOk());
        enviar(post("/atencion/" + id + "/finalizar"), medico, "{}").andExpect(status().isOk());
    }

    @Test
    void consultarTraeAntecedentes() throws Exception {
        Cita anterior = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        long id = iniciar(anterior);
        long formula = crearFormula(anterior.getId(), detalle(amoxicilina, 21));
        enviar(post("/formula-medica/" + formula + "/confirmar"), medico, "{}").andExpect(status().isOk());
        guardar(id, "");
        enviar(post("/atencion/" + id + "/finalizar"), medico, "{}").andExpect(status().isOk());
        AnotacionHistoria antigua = new AnotacionHistoria();
        antigua.setHistoria(historias.findAll().getFirst());
        antigua.setMedico(doctor);
        antigua.setFecha(LocalDateTime.now().minusYears(1));
        antigua.setDescripcion("Antigua");
        anotaciones.save(antigua);
        Cita nueva = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        consultar("/atencion/cita/" + nueva.getId(), medico).andExpect(jsonPath("$.atencion").isEmpty())
                .andExpect(jsonPath("$.mascota.especie").value("Perro"))
                .andExpect(jsonPath("$.propietario.documento").value("10"))
                .andExpect(jsonPath("$.antecedentes.atenciones.length()").value(1))
                .andExpect(jsonPath("$.antecedentes.atenciones[0].diagnostico").value("Otitis"))
                .andExpect(jsonPath("$.antecedentes.anotaciones.length()").value(1))
                .andExpect(jsonPath("$.antecedentes.anotaciones[0].descripcion").value("Antigua"))
                .andExpect(jsonPath("$.antecedentes.formulas[0].numero").value("FM-000001"));
        consultar("/atencion/cita/" + nueva.getId(), admin).andExpect(status().isOk());
    }

    // ------------------------------------------------------------ formulas

    @ParameterizedTest
    @CsvSource({"1,8,7,21", "0.5,12,5,5", "1.5,8,1,5", "1,168,1,1", "2,24,30,60", "0.33,8,7,7"})
    void cantidadSugerida(BigDecimal dosis, int frecuencia, int dias, int esperada) {
        assertEquals(esperada, FormulaRs.sugerida(dosis, frecuencia, dias));
    }

    @Test
    void formulaConVariosMedicamentosRecorreSusEstados() throws Exception {
        Cita cita = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        conflicto(enviar(post("/formula-medica/crear"), medico, cuerpo(cita.getId(), detalle(amoxicilina, 21))),
                "durante la atención");
        iniciar(cita);
        JsonNode f = leer(enviar(post("/formula-medica/crear"), medico, cuerpo(cita.getId(),
                detalle(amoxicilina, 25) + "," + detalle(jarabe, null))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoFormula").value("BORRADOR"))
                .andExpect(jsonPath("$.data.numero").isEmpty()).andExpect(jsonPath("$.data.estadoDespacho").isEmpty())
                .andExpect(jsonPath("$.data.detalles[0].cantidadSugerida").value(21))
                .andExpect(jsonPath("$.data.detalles[0].cantidadPrescrita").value(25))
                .andExpect(jsonPath("$.data.detalles[0].cantidadDisponibleVigente").value(30))
                .andExpect(jsonPath("$.data.detalles[0].alcanza").value(true))
                .andExpect(jsonPath("$.data.detalles[1].cantidadPrescrita").value(21))
                .andExpect(jsonPath("$.data.detalles[1].alcanza").value(false)));
        long id = f.get("data").get("id").asLong();
        enviar(put("/formula-medica/" + id), medico, cuerpo(cita.getId(), detalle(jarabe, 2)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.detalles.length()").value(1));
        consultar("/formula-medica/" + id, token("DESPACHO_MEDICO"), 403);
        enviar(post("/formula-medica/" + id + "/confirmar"), medico, "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.numero").value("FM-000001"))
                .andExpect(jsonPath("$.data.estadoDespacho").value("PENDIENTE_DESPACHO"));
        conflicto(enviar(put("/formula-medica/" + id), medico, cuerpo(cita.getId(), detalle(jarabe, 3))), "ya no se modifica");
        conflicto(enviar(post("/formula-medica/" + id + "/confirmar"), medico, "{}"), "ya no se modifica");
        consultar("/formula-medica/" + id, token("DESPACHO_MEDICO")).andExpect(jsonPath("$.numero").value("FM-000001"));
        assertEquals(30, lotes.existencias(amoxicilina.getId()), "Formular no descuenta inventario");
        assertEquals(0, movimientos.count());
        enviar(post("/formula-medica/" + id + "/anular"), medico, "{}").andExpect(status().isBadRequest());
        enviar(post("/formula-medica/" + id + "/anular"), admin, "{\"motivo\":\"Duplicada\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoFormula").value("ANULADA"))
                .andExpect(jsonPath("$.data.estadoDespacho").value("CANCELADO"));
        conflicto(enviar(post("/formula-medica/" + id + "/anular"), medico, "{\"motivo\":\"x\"}"), "ya está anulada");
        assertEquals(3, formulaHistorial.count());
        consultar("/formula-medica/listar?estado=ANULADA&texto=LUNA", medico).andExpect(jsonPath("$.length()").value(1));
        consultar("/formula-medica/listar?estado=CONFIRMADA", medico).andExpect(jsonPath("$.length()").value(0));
        consultar("/formula-medica/cita/" + cita.getId(), medico).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void formulaValidaDetalles() throws Exception {
        Cita cita = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        iniciar(cita);
        for (String malo : new String[]{detalle(amoxicilina, 0), detalle(amoxicilina, 1).replace("\"dosis\":1", "\"dosis\":0"),
                detalle(amoxicilina, 1).replace("\"frecuenciaHoras\":8", "\"frecuenciaHoras\":169"),
                detalle(amoxicilina, 1).replace("\"duracionDias\":7", "\"duracionDias\":0"),
                detalle(amoxicilina, 1).replace("ORAL", "NASAL"), ""}) {
            enviar(post("/formula-medica/crear"), medico, cuerpo(cita.getId(), malo)).andExpect(status().isBadRequest());
        }
        enviar(post("/formula-medica/crear"), medico, cuerpo(cita.getId(),
                detalle(amoxicilina, 1) + "," + detalle(amoxicilina, 2))).andExpect(status().isConflict());
        amoxicilina.setActivo(false);
        medicamentos.save(amoxicilina);
        enviar(post("/formula-medica/crear"), medico, cuerpo(cita.getId(), detalle(amoxicilina, 1)))
                .andExpect(status().isConflict());
        assertEquals(0, formulas.count(), "Un detalle invalido no deja encabezados");
    }

    @Test
    void formulaAjenaSeRechaza() throws Exception {
        Cita cita = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        iniciar(cita);
        long id = crearFormula(cita.getId(), detalle(amoxicilina, 1));
        Usuario otro = usuario("MEDICO");
        medico(otro, "2");
        String ajeno = token(otro);
        consultar("/formula-medica/" + id, ajeno, 403);
        enviar(post("/formula-medica/" + id + "/confirmar"), ajeno, "{}").andExpect(status().isForbidden());
        enviar(post("/formula-medica/crear"), ajeno, cuerpo(cita.getId(), detalle(amoxicilina, 1)))
                .andExpect(status().isForbidden());
        consultar("/formula-medica/listar", ajeno).andExpect(jsonPath("$.length()").value(0));
        consultar("/formula-medica/listar", admin).andExpect(jsonPath("$.length()").value(1));
    }

    // ------------------------------------------------------------ PDF

    @Test
    void pdfDeFormulaYPrefactura() throws Exception {
        Cita cita = cita(LocalDateTime.now(), EstadoCita.CONFIRMADA);
        long atencion = iniciar(cita);
        long id = crearFormula(cita.getId(), detalle(amoxicilina, 10) + "," + detalle(jarabe, 2));
        assertTrue(pdf("/formula-medica/" + id + "/pdf").contains("BORRADOR"));
        enviar(post("/formula-medica/" + id + "/confirmar"), medico, "{}").andExpect(status().isOk());
        String formula = pdf("/formula-medica/" + id + "/pdf");
        for (String texto : new String[]{"Clínica Prueba", "FM-000001", "Amoxicilina", "Cada 8 horas", "7 días", "Luna",
                "Eva Paz", "Con comida", "RP1"}) {
            assertTrue(formula.contains(texto), texto + " en " + formula);
        }
        guardar(atencion, ",\"servicios\":[{\"servicioId\":" + consulta.getId() + ",\"cantidad\":1}]");
        // Servicio 50000 + 9500; amoxicilina 15000; jarabe 20000 + 3800.
        String prefactura = pdf("/atencion/" + atencion + "/prefactura/pdf");
        for (String texto : new String[]{"PREFACTURA", "no válido como factura", "$ 85.000,00", "$ 13.300,00", "$ 98.300,00"}) {
            assertTrue(prefactura.contains(texto), texto + " en " + prefactura);
        }
        consultar("/formula-medica/99999/pdf", medico, 404);
    }

    private String pdf(String ruta) throws Exception {
        byte[] contenido = mvc.perform(get(ruta).header("Authorization", medico)).andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", containsString(".pdf")))
                .andReturn().getResponse().getContentAsByteArray();
        try (PdfReader lector = new PdfReader(contenido)) {
            return new PdfTextExtractor(lector).getTextFromPage(1).replaceAll("\\s+", " ");
        }
    }

    // ------------------------------------------------------------ apoyo

    private ResultActions enviarIniciar(Cita cita, String token) throws Exception {
        return mvc.perform(post("/atencion/iniciar/" + cita.getId()).header("Authorization", token));
    }

    private long iniciar(Cita cita) throws Exception {
        return leer(enviarIniciar(cita, medico).andExpect(status().isOk())).get("data").get("id").asLong();
    }

    private ResultActions guardar(long id, String extra) throws Exception {
        return enviar(put("/atencion/" + id), medico, """
                {"motivoConsulta":"Dolor","sintomas":"Se rasca","diagnostico":"Otitis","observaciones":"Leve",
                 "recomendaciones":"Reposo"%s}""".formatted(extra));
    }

    private long crearFormula(Long citaId, String detalle) throws Exception {
        return leer(enviar(post("/formula-medica/crear"), medico, cuerpo(citaId, detalle)).andExpect(status().isOk()))
                .get("data").get("id").asLong();
    }

    private static String cuerpo(Long citaId, String detalle) {
        return "{\"citaId\":" + citaId + ",\"indicacionesGenerales\":\"Reposo\",\"detalles\":[" + detalle + "]}";
    }

    private static String detalle(Medicamento m, Integer cantidad) {
        return """
                {"medicamentoId":%d,"dosis":1,"unidadDosis":"tableta","frecuenciaHoras":8,"duracionDias":7,
                 "viaAdministracion":"ORAL","cantidadPrescrita":%s,"indicaciones":"Con comida"}"""
                .formatted(m.getId(), cantidad);
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, String cuerpo) throws Exception {
        return mvc.perform(peticion.header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    private ResultActions consultar(String ruta, String token) throws Exception {
        return consultar(ruta, token, 200);
    }

    private ResultActions consultar(String ruta, String token, int estado) throws Exception {
        return mvc.perform(get(ruta).header("Authorization", token)).andExpect(status().is(estado));
    }

    private void conflicto(ResultActions r, String mensaje) throws Exception {
        r.andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString(mensaje)));
    }

    private JsonNode leer(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private Cita cita(LocalDateTime inicio, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setCliente(luna.getCliente());
        cita.setMascota(luna);
        cita.setMedico(doctor);
        cita.setFechaInicio(inicio.withNano(0));
        cita.setFechaFin(inicio.withNano(0).plusMinutes(30));
        cita.setEstado(estado);
        cita.setMotivo("Control");
        return citas.save(cita);
    }

    private Impuesto impuesto(String nombre, int porcentaje) {
        Impuesto i = new Impuesto();
        i.setNombre(nombre);
        i.setPorcentaje(BigDecimal.valueOf(porcentaje));
        return impuestos.save(i);
    }

    private Servicio servicio(String codigo, TipoServicio tipo, Especializacion e) {
        Servicio s = new Servicio();
        s.setCodigo(codigo);
        s.setNombre("Servicio " + codigo);
        s.setTipoServicio(tipo);
        s.setEspecializacion(e);
        return servicios.save(s);
    }

    private void tarifa(Servicio s, int valor, Impuesto i) {
        Tarifa t = new Tarifa();
        t.setServicio(s);
        t.setValor(BigDecimal.valueOf(valor));
        t.setImpuesto(i);
        t.setVigenteDesde(LocalDate.now().minusDays(10));
        tarifas.save(t);
    }

    private Medicamento medicamento(String codigo, String nombre, int precio, Impuesto i, int existencias) {
        Medicamento m = new Medicamento();
        m.setCodigo(codigo);
        m.setNombre(nombre);
        m.setRegistroInvima("I");
        m.setPrecioVenta(BigDecimal.valueOf(precio));
        m.setImpuesto(i);
        m.setFechaCreacionRegistro(LocalDateTime.now());
        m = medicamentos.save(m);
        LoteMedicamento l = new LoteMedicamento();
        l.setMedicamento(m);
        l.setNumeroLote("L");
        l.setPrecioCompra(BigDecimal.ONE);
        l.setCantidadInicial(Math.max(existencias, 5));
        l.setCantidadDisponible(existencias == 0 ? 5 : existencias);
        l.setFechaCompra(LocalDate.now().minusDays(90));
        l.setFechaIngreso(LocalDate.now().minusDays(90));
        // Sin existencias vigentes: lo unico que hay esta vencido.
        l.setFechaVencimiento(existencias == 0 ? LocalDate.now().minusDays(1) : LocalDate.now().plusDays(90));
        lotes.save(l);
        return m;
    }

    private Medico medico(Usuario usuario, String documento) {
        Especializacion e = new Especializacion();
        e.setNombre("E" + documento);
        e.setCodigoEspecializacion("C" + documento);
        Medico m = new Medico();
        m.setTipoDocumento("CC");
        m.setNumeroDocumento(documento);
        m.setNombres("Juan");
        m.setApellidos("Rojas");
        m.setRegistroProfesional("RP" + documento);
        m.setEspecializacion(especializaciones.save(e));
        m.setEstado(true);
        m.setUsuarioId(usuario.getId());
        return medicos.save(m);
    }

    private Mascota mascota() {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento("10");
        cliente.setNombres("Eva");
        cliente.setApellidos("Paz");
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setActivo(true);
        Raza raza = new Raza();
        raza.setNombre("Criollo");
        raza.setEspecie("Perro");
        raza.setFechaCreacion(LocalDateTime.now());
        Mascota m = new Mascota();
        m.setNombreMascota("Luna");
        m.setEdad(3);
        m.setCliente(clientes.save(cliente));
        m.setRaza(razas.save(raza));
        m.setActivo(true);
        return mascotas.save(m);
    }
}
