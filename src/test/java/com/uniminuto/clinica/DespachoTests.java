package com.uniminuto.clinica;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.entity.enums.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Despacho y dispensacion (R5, 5.7). */
class DespachoTests extends PruebaIntegracion {

    @Autowired ConfiguracionClinicaRepository configuracion;
    @Autowired ImpuestoRepository impuestos;
    @Autowired MedicamentoRepository medicamentos;
    @Autowired LoteMedicamentoRepository lotes;
    @Autowired MovimientoInventarioRepository movimientos;
    @Autowired FormulaMedicaRepository formulas;
    @Autowired FormulaMedicaDetalleRepository detalles;
    @Autowired FormulaHistorialEstadoRepository historial;
    @Autowired DespachoRepository despachos;
    @Autowired DespachoDetalleRepository despachoDetalles;
    @Autowired CitaRepository citas;
    @Autowired MedicoRepository medicos;
    @Autowired EspecializacionRepository especializaciones;
    @Autowired MascotaRepository mascotas;
    @Autowired RazaRepository razas;
    @Autowired ClienteRepository clientes;
    @Autowired ObjectMapper json;

    private String despacho;
    private Usuario usuarioDespacho;
    private Cita cita;
    private Medicamento amoxicilina;
    private Medicamento jarabe;
    private LoteMedicamento viejo;
    private LoteMedicamento nuevo;
    private LoteMedicamento vencido;
    private FormulaMedica formula;
    private FormulaMedicaDetalle dAmoxicilina;
    private FormulaMedicaDetalle dJarabe;

    @BeforeEach
    void preparar() {
        usuarioDespacho = usuario("DESPACHO_MEDICO");
        despacho = token(usuarioDespacho);
        ConfiguracionClinica c = new ConfiguracionClinica();
        c.setNombre("Clínica Prueba");
        c.setNit("900");
        c.setPrefijoFactura("FV");
        c.setPrefijoFormula("FM");
        c.setPrefijoDespacho("DP");
        configuracion.save(c);
        cita = cita();
        amoxicilina = medicamento("AMX00001", "Amoxicilina");
        jarabe = medicamento("JAR00001", "Jarabe");
        nuevo = lote(amoxicilina, "NUEVO", 50, 200);
        viejo = lote(amoxicilina, "VIEJO", 10, 20);
        vencido = lote(amoxicilina, "VENC", 40, -1);
        formula = formula("FM-000001");
        dAmoxicilina = detalle(formula, amoxicilina, 21);
        dJarabe = detalle(formula, jarabe, 2);
    }

    @Override
    protected void limpiarDatos() {
        movimientos.deleteAll();
        despachoDetalles.deleteAllInBatch();
        despachos.deleteAllInBatch();
        historial.deleteAllInBatch();
        detalles.deleteAllInBatch();
        formulas.deleteAllInBatch();
        lotes.deleteAllInBatch();
        medicamentos.deleteAllInBatch();
        impuestos.deleteAllInBatch();
        configuracion.deleteAllInBatch();
        citas.deleteAllInBatch();
        medicos.deleteAllInBatch();
        especializaciones.deleteAllInBatch();
        mascotas.deleteAllInBatch();
        razas.deleteAllInBatch();
        clientes.deleteAllInBatch();
    }

    @Test
    void bandejaBuscaSinTildesYFiltra() throws Exception {
        pendientes("").andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].numero").value("FM-000001"))
                .andExpect(jsonPath("$[0].totalMedicamentos").value(2))
                .andExpect(jsonPath("$[0].medicamentos[0].cantidadPrescrita").value(21));
        pendientes("?texto=pérez").andExpect(jsonPath("$.length()").value(1));
        pendientes("?texto=LUNA").andExpect(jsonPath("$.length()").value(1));
        pendientes("?numero=000001").andExpect(jsonPath("$.length()").value(1));
        pendientes("?texto=nadie").andExpect(jsonPath("$.length()").value(0));
        pendientes("?estado=DESPACHADO").andExpect(jsonPath("$.length()").value(0));
        pendientes("?hasta=" + LocalDate.now().minusDays(1)).andExpect(jsonPath("$.length()").value(0));
        formula.setEstadoFormula(EstadoFormula.ANULADA);
        formula.setEstadoDespacho(EstadoDespacho.CANCELADO);
        formulas.save(formula);
        pendientes("").andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void sugerenciaFefoExcluyeVencidos() throws Exception {
        consultar("/despacho/formula/" + formula.getId())
                .andExpect(jsonPath("$.despachoId").isEmpty())
                .andExpect(jsonPath("$.detalles[0].existenciaTotalVigente").value(60))
                .andExpect(jsonPath("$.detalles[0].lotes[0].numeroLote").value("VIEJO"))
                .andExpect(jsonPath("$.detalles[0].lotes[0].cantidadSugerida").value(10))
                .andExpect(jsonPath("$.detalles[0].lotes[0].proximoVencer").value(true))
                .andExpect(jsonPath("$.detalles[0].lotes[1].numeroLote").value("NUEVO"))
                .andExpect(jsonPath("$.detalles[0].lotes[1].cantidadSugerida").value(11))
                .andExpect(jsonPath("$.detalles[0].lotes[2].vencido").value(true))
                .andExpect(jsonPath("$.detalles[0].lotes[2].cantidadSugerida").value(0))
                .andExpect(jsonPath("$.detalles[1].puedeEntregarse").value(false));
    }

    @Test
    void prepararEsDeUnSoloUsuario() throws Exception {
        long id = preparar(formula);
        assertEquals(id, preparar(formula));
        assertEquals(1, despachos.count());
        String otro = token("DESPACHO_MEDICO");
        enviar(post("/despacho/formula/" + formula.getId() + "/preparar"), otro, "{}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString(usuarioDespacho.getUsername())));
        confirmar(id, otro, null, item(dAmoxicilina, viejo, 1)).andExpect(status().isForbidden());
        pendientes("?estado=EN_PREPARACION").andExpect(jsonPath("$[0].usuarioPreparacion")
                .value(usuarioDespacho.getUsername()));
        sinCambios();
    }

    @Test
    void despachoCompletoDescuentaYDejaMovimientos() throws Exception {
        lote(jarabe, "J1", 5, 100);
        LoteMedicamento j = lotes.findAll().getLast();
        long id = preparar(formula);
        confirmar(id, despacho, null, item(dAmoxicilina, viejo, 10), item(dAmoxicilina, nuevo, 11), item(dJarabe, j, 2))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.data.estadoDespacho").value("DESPACHADO"))
                .andExpect(jsonPath("$.data.numero").value("DP-000001"))
                .andExpect(jsonPath("$.data.items.length()").value(3));
        assertEquals(0, disponible(viejo));
        assertEquals(39, disponible(nuevo));
        assertEquals(3, disponible(j));
        List<MovimientoInventario> salidas = movimientos.findByDespacho_IdAndTipo(id, TipoMovimiento.SALIDA_DESPACHO);
        assertEquals(3, salidas.size());
        assertTrue(salidas.stream().allMatch(m -> m.getFormula() != null && m.getDespachoDetalle() != null));
        assertEquals(90, salidas.get(0).getSaldoProductoResultante());
        assertEquals(79, salidas.get(1).getSaldoProductoResultante());
        DespachoDetalle d = despachoDetalles.findByDespacho_IdInOrderById(List.of(id)).getFirst();
        assertEquals(0, d.getPrecioVentaUnitario().compareTo(BigDecimal.valueOf(1500)));
        assertEquals(0, d.getPorcentajeIva().compareTo(BigDecimal.valueOf(19)));
        assertEquals(21, detalles.findById(dAmoxicilina.getId()).orElseThrow().getCantidadDespachada());
        pendientes("").andExpect(jsonPath("$.length()").value(0));
        enviar(post("/despacho/formula/" + formula.getId() + "/preparar"), despacho, "{}").andExpect(status().isConflict());
    }

    @Test
    void parcialYLuegoCompleto() throws Exception {
        long id = preparar(formula);
        confirmar(id, despacho, null, item(dAmoxicilina, viejo, 5)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoDespacho").value("DESPACHO_PARCIAL"))
                .andExpect(jsonPath("$.data.sinExistencias[0]").value("Jarabe"));
        consultar("/despacho/formula/" + formula.getId())
                .andExpect(jsonPath("$.detalles[0].cantidadPendiente").value(16))
                .andExpect(jsonPath("$.detalles[0].lotes[0].cantidadSugerida").value(5));
        long segundo = preparar(formula);
        assertNotEquals(id, segundo);
        conflicto(confirmar(segundo, despacho, null, item(dAmoxicilina, nuevo, 17)), "solo hay 16 pendientes");
        LoteMedicamento j = lote(jarabe, "J1", 2, 100);
        confirmar(segundo, despacho, null, item(dAmoxicilina, nuevo, 16), item(dJarabe, j, 2)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoDespacho").value("DESPACHADO"));
        consultar("/despacho/historial?texto=luna").andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].items.length()").value(2));
        consultar("/despacho/formula/" + formula.getId() + "/historial-estados")
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[1].estadoNuevo").value("DESPACHO_PARCIAL"))
                .andExpect(jsonPath("$[3].usuario").value(usuarioDespacho.getUsername()));
    }

    @Test
    void cualquierItemInvalidoImpideTodo() throws Exception {
        long id = preparar(formula);
        conflicto(confirmar(id, despacho, null, item(dAmoxicilina, vencido, 1)), "vencido");
        confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 5), item(dAmoxicilina, viejo, 11))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detalles[0].campo").value("items[1]"));
        conflicto(confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 22)), "pendientes");
        conflicto(confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 12), item(dAmoxicilina, viejo, 10)), "pendientes");
        conflicto(confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 5), item(dAmoxicilina, nuevo, 5)), "repetido");
        conflicto(confirmar(id, despacho, null, item(dJarabe, nuevo, 1)), "no es de Jarabe");
        FormulaMedica otra = formula("FM-000002");
        conflicto(confirmar(id, despacho, null, item(detalle(otra, amoxicilina, 5), nuevo, 1)), "no pertenece");
        confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 0)).andExpect(status().isBadRequest());
        confirmar(id, despacho, null, item(dAmoxicilina, nuevo, -3)).andExpect(status().isBadRequest());
        nuevo.setActivo(false);
        lotes.save(nuevo);
        conflicto(confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 1)), "inactivo");
        sinCambios();
        assertEquals(EstadoPreparacion.EN_PREPARACION, despachos.findById(id).orElseThrow().getEstado());
    }

    @Test
    void confirmarDosVecesNoDescuentaDosVeces() throws Exception {
        long id = preparar(formula);
        confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 5)).andExpect(status().isOk());
        confirmar(id, despacho, null, item(dAmoxicilina, nuevo, 5)).andExpect(status().isConflict());
        assertEquals(45, disponible(nuevo));
        assertEquals(1, movimientos.count());
    }

    @Test
    void idempotencyKeyDevuelveElMismoResultado() throws Exception {
        long id = preparar(formula);
        for (int i = 0; i < 3; i++) {
            confirmar(id, despacho, "clave-1", item(dAmoxicilina, nuevo, 5)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(id)).andExpect(jsonPath("$.data.items[0].cantidad").value(5));
        }
        assertEquals(45, disponible(nuevo));
        long segundo = preparar(formula);
        conflicto(confirmar(segundo, despacho, "clave-1", item(dAmoxicilina, nuevo, 5)), "idempotencia");
        confirmar(segundo, despacho, "x".repeat(65), item(dAmoxicilina, nuevo, 5)).andExpect(status().isBadRequest());
        assertEquals(1, movimientos.count());
    }

    @Test
    void confirmacionesSimultaneasSoloDescuentanUnaVez() throws Exception {
        long id = preparar(formula);
        List<Callable<Integer>> tareas = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            tareas.add(() -> confirmar(id, despacho, null, item(dAmoxicilina, viejo, 10)).andReturn().getResponse().getStatus());
        }
        int exitosas = 0;
        try (var hilos = Executors.newFixedThreadPool(6)) {
            for (Future<Integer> f : hilos.invokeAll(tareas)) {
                exitosas += f.get() == 200 ? 1 : 0;
            }
        }
        assertEquals(1, exitosas);
        assertEquals(0, disponible(viejo));
        assertEquals(1, movimientos.count());
    }

    @Test
    void sinExistenciasQuedaPendientePorDisponibilidad() throws Exception {
        FormulaMedica agotada = formula("FM-000002");
        detalle(agotada, jarabe, 3);
        long id = preparar(agotada);
        confirmar(id, despacho, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoDespacho").value("PENDIENTE_DISPONIBILIDAD"))
                .andExpect(jsonPath("$.data.items.length()").value(0));
        assertEquals(0, movimientos.count());
        pendientes("?estado=PENDIENTE_DISPONIBILIDAD").andExpect(jsonPath("$.length()").value(1));
        enviar(post("/despacho/" + id + "/revertir"), token("ADMINISTRADOR"), "{\"motivo\":\"x\"}")
                .andExpect(status().isConflict());
        long conStock = preparar(formula);
        conflicto(confirmar(conStock, despacho, null), "Hay existencias");
    }

    @Test
    void revertirDevuelveExistenciasYConservaTodo() throws Exception {
        long id = preparar(formula);
        confirmar(id, despacho, null, item(dAmoxicilina, viejo, 10), item(dAmoxicilina, nuevo, 3)).andExpect(status().isOk());
        String admin = token("ADMINISTRADOR");
        enviar(post("/despacho/" + id + "/revertir"), admin, "{}").andExpect(status().isBadRequest());
        enviar(post("/despacho/" + id + "/revertir"), admin, "{\"motivo\":\"Error de entrega\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.revertido").value(true))
                .andExpect(jsonPath("$.data.motivoReversion").value("Error de entrega"))
                .andExpect(jsonPath("$.data.estadoDespacho").value("PENDIENTE_DESPACHO"));
        assertEquals(10, disponible(viejo));
        assertEquals(50, disponible(nuevo));
        assertEquals(0, detalles.findById(dAmoxicilina.getId()).orElseThrow().getCantidadDespachada());
        assertEquals(4, movimientos.count());
        assertEquals(2, despachoDetalles.count());
        List<MovimientoInventario> entradas = movimientos.findByDespacho_IdAndTipo(id, TipoMovimiento.ENTRADA_REVERSION);
        assertTrue(entradas.stream().allMatch(m -> m.getMovimientoRevertido() != null));
        assertEquals(100, entradas.getLast().getSaldoProductoResultante());
        conflicto(enviar(post("/despacho/" + id + "/revertir"), admin, "{\"motivo\":\"x\"}"), "REVERTIDO");
        consultar("/despacho/medicamento/" + amoxicilina.getId() + "/historial")
                .andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].movimientoId").isNumber())
                .andExpect(jsonPath("$[0].movimientoReversionId").isNumber())
                .andExpect(jsonPath("$[0].estado").value("REVERTIDO"));
        assertTrue(pdf(id).contains("REVERTIDO"));
        preparar(formula);
    }

    @Test
    void noSeRevierteLoFacturado() throws Exception {
        long id = preparar(formula);
        confirmar(id, despacho, null, item(dAmoxicilina, viejo, 1)).andExpect(status().isOk());
        DespachoDetalle d = despachoDetalles.findAll().getFirst();
        d.setFacturado(true);
        despachoDetalles.save(d);
        conflicto(enviar(post("/despacho/" + id + "/revertir"), token("ADMINISTRADOR"), "{\"motivo\":\"x\"}"), "facturados");
        assertEquals(9, disponible(viejo));
    }

    @Test
    void cancelarSegunRolYEntregas() throws Exception {
        String ruta = "/despacho/formula/" + formula.getId() + "/cancelar";
        long id = preparar(formula);
        enviar(post(ruta), token("DESPACHO_MEDICO"), "{\"motivo\":\"x\"}").andExpect(status().isForbidden());
        confirmar(id, despacho, null, item(dAmoxicilina, viejo, 1)).andExpect(status().isOk());
        enviar(post(ruta), despacho, "{\"motivo\":\"x\"}").andExpect(status().isForbidden());
        enviar(post(ruta), token("ADMINISTRADOR"), "{\"motivo\":\"No vuelve\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoDespacho").value("CANCELADO"));
        enviar(post(ruta), token("ADMINISTRADOR"), "{\"motivo\":\"x\"}").andExpect(status().isConflict());
        // Revertir despues de cancelar devuelve existencias sin reabrir el despacho.
        enviar(post("/despacho/" + id + "/revertir"), token("ADMINISTRADOR"), "{\"motivo\":\"x\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoDespacho").value("CANCELADO"));
        FormulaMedica otra = formula("FM-000002");
        detalle(otra, amoxicilina, 5);
        preparar(otra);
        enviar(post("/despacho/formula/" + otra.getId() + "/cancelar"), despacho, "{\"motivo\":\"Pedido\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.despachoId").isEmpty());
    }

    @Test
    void liberarDejaLaFormulaDisponibleParaOtro() throws Exception {
        long id = preparar(formula);
        String otro = token("DESPACHO_MEDICO");
        enviar(post("/despacho/" + id + "/liberar"), otro, "{}").andExpect(status().isForbidden());
        enviar(post("/despacho/" + id + "/liberar"), token("ADMINISTRADOR"), "{}").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estadoDespacho").value("PENDIENTE_DESPACHO"));
        enviar(post("/despacho/formula/" + formula.getId() + "/preparar"), otro, "{}").andExpect(status().isOk());
        conflicto(confirmar(id, despacho, null, item(dAmoxicilina, viejo, 1)), "CANCELADO");
        sinCambios();
    }

    @Test
    void comprobantePdf() throws Exception {
        long id = preparar(formula);
        mvc.perform(get("/despacho/" + id + "/comprobante/pdf").header("Authorization", despacho))
                .andExpect(status().isConflict());
        confirmar(id, despacho, null, item(dAmoxicilina, viejo, 4)).andExpect(status().isOk());
        String texto = pdf(id);
        for (String t : new String[]{"DP-000001", "FM-000001", "Amoxicilina", "VIEJO", "Juan Pérez", "Luna", "21 4 17",
                "Jarabe - - 2 0 2", usuarioDespacho.getUsername()}) {
            assertTrue(texto.contains(t), t + " en " + texto);
        }
    }

    @Test
    void formulaAnuladaConPreparacionAbiertaNoSeDespacha() throws Exception {
        long id = preparar(formula);
        enviar(post("/formula-medica/" + formula.getId() + "/anular"), token("ADMINISTRADOR"), "{\"motivo\":\"Error\"}")
                .andExpect(status().isOk());
        conflicto(confirmar(id, despacho, null, item(dAmoxicilina, viejo, 1)), "CANCELADO");
        enviar(post("/despacho/formula/" + formula.getId() + "/preparar"), despacho, "{}").andExpect(status().isConflict());
        sinCambios();
    }

    @ParameterizedTest
    @CsvSource({"ADMINISTRADOR,GET,/despacho/formulas-pendientes", "ADMINISTRADOR,GET,/despacho/formula/1",
            "ADMINISTRADOR,POST,/despacho/formula/1/preparar", "ADMINISTRADOR,POST,/despacho/1/confirmar",
            "MEDICO,GET,/despacho/historial", "SECRETARIA,GET,/despacho/historial", "MEDICO,POST,/despacho/1/confirmar",
            "DESPACHO_MEDICO,POST,/despacho/1/revertir", "DESPACHO_MEDICO,POST,/formula-medica/crear",
            "DESPACHO_MEDICO,PUT,/formula-medica/1", "SECRETARIA,POST,/despacho/formula/1/cancelar"})
    void permisos(String rol, String metodo, String ruta) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(metodo), ruta).header("Authorization", token(rol))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }

    @Test
    void inexistentesYFiltrosInvalidos() throws Exception {
        confirmar(99999, despacho, null).andExpect(status().isNotFound());
        enviar(post("/despacho/formula/99999/preparar"), despacho, "{}").andExpect(status().isNotFound());
        mvc.perform(get("/despacho/historial?estado=XX").header("Authorization", despacho)).andExpect(status().isBadRequest());
        formula.setEstadoFormula(EstadoFormula.BORRADOR);
        formula.setEstadoDespacho(null);
        formulas.save(formula);
        mvc.perform(get("/despacho/formula/" + formula.getId()).header("Authorization", despacho))
                .andExpect(status().isNotFound());
        enviar(post("/despacho/formula/" + formula.getId() + "/preparar"), despacho, "{}").andExpect(status().isConflict());
    }

    // ------------------------------------------------------------ apoyo

    private void sinCambios() {
        assertEquals(50, disponible(nuevo));
        assertEquals(10, disponible(viejo));
        assertEquals(40, disponible(vencido));
        assertEquals(0, movimientos.count());
        assertEquals(0, detalles.findById(dAmoxicilina.getId()).orElseThrow().getCantidadDespachada());
    }

    private int disponible(LoteMedicamento l) {
        return lotes.findById(l.getId()).orElseThrow().getCantidadDisponible();
    }

    private long preparar(FormulaMedica f) throws Exception {
        JsonNode r = json.readTree(enviar(post("/despacho/formula/" + f.getId() + "/preparar"), despacho, "{}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.estadoDespacho").value("EN_PREPARACION"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        return r.get("data").get("despachoId").asLong();
    }

    private static String item(FormulaMedicaDetalle d, LoteMedicamento l, int cantidad) {
        return "{\"formulaDetalleId\":" + d.getId() + ",\"loteId\":" + l.getId() + ",\"cantidad\":" + cantidad + "}";
    }

    private ResultActions confirmar(long id, String token, String clave, String... items) throws Exception {
        MockHttpServletRequestBuilder peticion = post("/despacho/" + id + "/confirmar");
        if (clave != null) {
            peticion.header("Idempotency-Key", clave);
        }
        return enviar(peticion, token, "{\"observacion\":\"ok\",\"items\":[" + String.join(",", items) + "]}");
    }

    private ResultActions pendientes(String filtros) throws Exception {
        return consultar("/despacho/formulas-pendientes" + filtros);
    }

    private ResultActions consultar(String ruta) throws Exception {
        return mvc.perform(get(ruta).header("Authorization", despacho)).andExpect(status().isOk());
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, String cuerpo) throws Exception {
        return mvc.perform(peticion.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private void conflicto(ResultActions r, String mensaje) throws Exception {
        r.andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString(mensaje)));
    }

    private String pdf(long id) throws Exception {
        byte[] contenido = mvc.perform(get("/despacho/" + id + "/comprobante/pdf").header("Authorization", despacho))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"))
                .andReturn().getResponse().getContentAsByteArray();
        try (PdfReader lector = new PdfReader(contenido)) {
            return new PdfTextExtractor(lector).getTextFromPage(1).replaceAll("\\s+", " ");
        }
    }

    private FormulaMedica formula(String numero) {
        FormulaMedica f = new FormulaMedica();
        f.setNumero(numero);
        f.setCita(cita);
        f.setMedico(cita.getMedico());
        f.setMascota(cita.getMascota());
        f.setCliente(cita.getCliente());
        f.setEstadoFormula(EstadoFormula.CONFIRMADA);
        f.setEstadoDespacho(EstadoDespacho.PENDIENTE_DESPACHO);
        f.setFechaGeneracion(LocalDateTime.now());
        return formulas.save(f);
    }

    private FormulaMedicaDetalle detalle(FormulaMedica f, Medicamento m, int cantidad) {
        FormulaMedicaDetalle d = new FormulaMedicaDetalle();
        d.setFormula(f);
        d.setMedicamento(m);
        d.setDosis(BigDecimal.ONE);
        d.setUnidadDosis("tableta");
        d.setFrecuenciaHoras(8);
        d.setDuracionDias(7);
        d.setViaAdministracion(ViaAdministracion.ORAL);
        d.setCantidadPrescrita(cantidad);
        return detalles.save(d);
    }

    private Medicamento medicamento(String codigo, String nombre) {
        Impuesto i = new Impuesto();
        i.setNombre("IVA " + codigo);
        i.setPorcentaje(BigDecimal.valueOf(19));
        Medicamento m = new Medicamento();
        m.setCodigo(codigo);
        m.setNombre(nombre);
        m.setRegistroInvima("I");
        m.setPrecioVenta(BigDecimal.valueOf(1500));
        m.setImpuesto(impuestos.save(i));
        m.setFechaCreacionRegistro(LocalDateTime.now());
        return medicamentos.save(m);
    }

    private LoteMedicamento lote(Medicamento m, String numero, int cantidad, int diasVence) {
        LoteMedicamento l = new LoteMedicamento();
        l.setMedicamento(m);
        l.setNumeroLote(numero);
        l.setPrecioCompra(BigDecimal.valueOf(900));
        l.setCantidadInicial(cantidad);
        l.setCantidadDisponible(cantidad);
        l.setFechaCompra(LocalDate.now().minusDays(60));
        l.setFechaIngreso(LocalDate.now().minusDays(60));
        l.setFechaVencimiento(LocalDate.now().plusDays(diasVence));
        return lotes.save(l);
    }

    private Cita cita() {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento("10");
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
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
        Especializacion e = new Especializacion();
        e.setNombre("General");
        e.setCodigoEspecializacion("GEN");
        Medico medico = new Medico();
        medico.setTipoDocumento("CC");
        medico.setNumeroDocumento("1");
        medico.setNombres("Ana");
        medico.setApellidos("Rojas");
        medico.setRegistroProfesional("RP1");
        medico.setEspecializacion(especializaciones.save(e));
        medico.setEstado(true);
        Cita c = new Cita();
        c.setMascota(mascotas.save(m));
        c.setCliente(m.getCliente());
        c.setMedico(medicos.save(medico));
        c.setFechaInicio(LocalDateTime.now());
        c.setFechaFin(LocalDateTime.now().plusMinutes(30));
        c.setEstado(EstadoCita.ATENDIDA);
        c.setMotivo("Control");
        return citas.save(c);
    }
}
