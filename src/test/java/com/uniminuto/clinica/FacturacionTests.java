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

/** Facturacion y pagos (R3.4, 5.8). */
class FacturacionTests extends PruebaIntegracion {

    @Autowired ConfiguracionClinicaRepository configuracion;
    @Autowired ImpuestoRepository impuestos;
    @Autowired ServicioRepository servicios;
    @Autowired TarifaRepository tarifas;
    @Autowired TarifaHistorialRepository tarifaHistorial;
    @Autowired MedicamentoRepository medicamentos;
    @Autowired LoteMedicamentoRepository lotes;
    @Autowired MovimientoInventarioRepository movimientos;
    @Autowired FormulaMedicaRepository formulas;
    @Autowired FormulaMedicaDetalleRepository formulaDetalles;
    @Autowired FormulaHistorialEstadoRepository formulaHistorial;
    @Autowired DespachoRepository despachos;
    @Autowired DespachoDetalleRepository despachoDetalles;
    @Autowired FacturaRepository facturas;
    @Autowired FacturaDetalleRepository facturaDetalles;
    @Autowired FacturaHistorialEstadoRepository facturaHistorial;
    @Autowired PagoRepository pagos;
    @Autowired AtencionMedicaRepository atenciones;
    @Autowired AtencionServicioRepository atencionServicios;
    @Autowired HistoriaMedicaRepository historias;
    @Autowired CitaRepository citas;
    @Autowired MedicoRepository medicos;
    @Autowired EspecializacionRepository especializaciones;
    @Autowired MascotaRepository mascotas;
    @Autowired RazaRepository razas;
    @Autowired ClienteRepository clientes;
    @Autowired ObjectMapper json;

    private String admin;
    private String secretaria;
    private Usuario usuarioDespacho;
    private Cita cita;
    private Servicio consulta;
    private Servicio vacuna;
    private Tarifa tarifaConsulta;
    private Impuesto iva19;
    private Medicamento amoxicilina;
    private FormulaMedica formula;
    private FormulaMedicaDetalle formulado;
    private DespachoDetalle entregado;

    /** Consulta 50.000 + 19 %, vacuna 2 x 35.000 + 0 %, amoxicilina 10 x 1.500 + 19 % (formuladas 21). */
    @BeforeEach
    void preparar() {
        admin = token("ADMINISTRADOR");
        secretaria = token("SECRETARIA");
        usuarioDespacho = usuario("DESPACHO_MEDICO");
        ConfiguracionClinica c = new ConfiguracionClinica();
        c.setNombre("Clínica Prueba");
        c.setNit("900");
        c.setPrefijoFactura("FV");
        c.setPrefijoFormula("FM");
        c.setPrefijoDespacho("DP");
        configuracion.save(c);
        Impuesto iva0 = impuesto("IVA 0", 0);
        iva19 = impuesto("IVA 19", 19);
        consulta = servicio("CONS", "Consulta");
        vacuna = servicio("VAC", "Vacuna");
        tarifaConsulta = tarifa(consulta, 50000, iva19);
        tarifa(vacuna, 35000, iva0);
        cita = cita();
        AtencionMedica atencion = atencion(cita);
        prestar(atencion, consulta, 1);
        prestar(atencion, vacuna, 2);
        amoxicilina = new Medicamento();
        amoxicilina.setCodigo("AMX00001");
        amoxicilina.setNombre("Amoxicilina");
        amoxicilina.setRegistroInvima("I");
        amoxicilina.setPrecioVenta(BigDecimal.valueOf(1500));
        amoxicilina.setImpuesto(iva19);
        amoxicilina.setFechaCreacionRegistro(LocalDateTime.now());
        amoxicilina = medicamentos.save(amoxicilina);
        formula = formula(atencion);
        formulado = formulado(21);
        entregado = entregar(10, EstadoPreparacion.CONFIRMADO);
    }

    @Override
    protected void limpiarDatos() {
        facturaHistorial.deleteAllInBatch();
        pagos.deleteAllInBatch();
        facturaDetalles.deleteAllInBatch();
        facturas.deleteAllInBatch();
        movimientos.deleteAll();
        despachoDetalles.deleteAllInBatch();
        despachos.deleteAllInBatch();
        formulaHistorial.deleteAllInBatch();
        formulaDetalles.deleteAllInBatch();
        formulas.deleteAllInBatch();
        atencionServicios.deleteAllInBatch();
        atenciones.deleteAllInBatch();
        historias.deleteAllInBatch();
        citas.deleteAllInBatch();
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

    @Test
    void previsualizarSoloIncluyeLoDespachado() throws Exception {
        entregar(3, EstadoPreparacion.REVERTIDO);
        consultar("/factura/citas-pendientes").andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].citaId").value(cita.getId()))
                .andExpect(jsonPath("$[0].despachoPendiente").value(true));
        consultar("/factura/previsualizar/" + cita.getId()).andExpect(jsonPath("$.detalles.length()").value(3))
                .andExpect(jsonPath("$.detalles[0].itemRef").value("SERVICIO-" + consulta.getId()))
                .andExpect(jsonPath("$.detalles[2].itemRef").value("MEDICAMENTO-" + entregado.getId()))
                .andExpect(jsonPath("$.detalles[2].cantidad").value(10))
                .andExpect(jsonPath("$.subtotal").value(135000)).andExpect(jsonPath("$.iva").value(12350))
                .andExpect(jsonPath("$.total").value(147350)).andExpect(jsonPath("$.topeDescuento").value(10))
                .andExpect(jsonPath("$.advertencias[0]").value(containsString("no despachados no se facturan")));
        assertEquals(0, facturas.count());
    }

    @Test
    void generarCongelaYNoSeRepite() throws Exception {
        long id = generar(secretaria, "");
        mvc.perform(get("/factura/" + id).header("Authorization", secretaria)).andExpect(jsonPath("$.numero").value("FV-000001"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE")).andExpect(jsonPath("$.total").value(147350))
                .andExpect(jsonPath("$.saldo").value(147350)).andExpect(jsonPath("$.detalles.length()").value(3))
                .andExpect(jsonPath("$.clienteDocumento").value("CC 10"));
        assertTrue(despachoDetalles.findById(entregado.getId()).orElseThrow().getFacturado());
        conflicto(enviar(post("/factura/generar"), secretaria, cuerpo("")), "ya tiene una factura vigente");
        consultar("/factura/previsualizar/" + cita.getId(), 409);
        consultar("/factura/citas-pendientes").andExpect(jsonPath("$.length()").value(0));
        // Los cambios posteriores no alteran lo facturado.
        enviar(put("/tarifa/" + tarifaConsulta.getId()), admin, "{\"valor\":90000,\"impuestoId\":" + iva19.getId()
                + ",\"motivo\":\"Alza\"}").andExpect(status().isOk());
        amoxicilina.setPrecioVenta(BigDecimal.valueOf(9999));
        medicamentos.save(amoxicilina);
        Cliente cliente = cita.getCliente();
        cliente.setNombres("Otro");
        clientes.save(cliente);
        consultar("/factura/" + id).andExpect(jsonPath("$.total").value(147350))
                .andExpect(jsonPath("$.clienteNombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(50000))
                .andExpect(jsonPath("$.detalles[2].precioUnitario").value(1500));
    }

    @Test
    void descuentosConTopeYRedondeo() throws Exception {
        String servicio = "SERVICIO-" + consulta.getId();
        enviar(post("/factura/generar"), secretaria, cuerpo(descuento(servicio, "10.01", "x"))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0].campo").value("descuentos[0].porcentaje"));
        enviar(post("/factura/generar"), secretaria, cuerpo(descuento("SERVICIO-999", "5", "x"))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0].campo").value("descuentos[0].itemRef"));
        enviar(post("/factura/generar"), secretaria, cuerpo(descuento(servicio, "5", ""))).andExpect(status().isBadRequest());
        enviar(post("/factura/generar"), admin, cuerpo(descuento(servicio, "100.5", "x"))).andExpect(status().isBadRequest());
        enviar(post("/factura/generar"), admin, cuerpo(descuento(servicio, "5", "x") + "," + descuento(servicio, "6", "x")))
                .andExpect(status().isConflict());
        assertEquals(0, facturas.count());
        // 50.000 - 33,33 % = 33.335; IVA 6.333,65. 15.000 - 10 % = 13.500; IVA 2.565. Vacunas 70.000.
        enviar(post("/factura/generar"), admin, cuerpo(descuento(servicio, "33.33", "Convenio") + ","
                + descuento("MEDICAMENTO-" + entregado.getId(), "10", "Cortesía"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.subtotal").value(135000)).andExpect(jsonPath("$.data.descuentos").value(18165))
                .andExpect(jsonPath("$.data.iva").value(8898.65)).andExpect(jsonPath("$.data.total").value(125733.65))
                .andExpect(jsonPath("$.data.detalles[0].motivoDescuento").value("Convenio"));
    }

    @Test
    void descuentoTotalQuedaPagada() throws Exception {
        String todos = descuento("SERVICIO-" + consulta.getId(), "100", "x") + ","
                + descuento("SERVICIO-" + vacuna.getId(), "100", "x") + ","
                + descuento("MEDICAMENTO-" + entregado.getId(), "100", "x");
        enviar(post("/factura/generar"), admin, cuerpo(todos)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0)).andExpect(jsonPath("$.data.estado").value("PAGADA"));
    }

    @Test
    void soloCitasAtendidasConAtencionFinalizadaYTarifa() throws Exception {
        tarifaConsulta.setActiva(false);
        tarifas.save(tarifaConsulta);
        consultar("/factura/previsualizar/" + cita.getId()).andExpect(jsonPath("$.advertencias[1]").value(containsString("tarifa")));
        conflicto(enviar(post("/factura/generar"), secretaria, cuerpo("")), "no tiene tarifa vigente");
        AtencionMedica a = atenciones.findByCita_Id(cita.getId()).orElseThrow();
        a.setFinalizada(false);
        atenciones.save(a);
        conflicto(enviar(post("/factura/generar"), secretaria, cuerpo("")), "atención finalizada");
        cita.setEstado(EstadoCita.EN_ATENCION);
        citas.save(cita);
        conflicto(enviar(post("/factura/generar"), secretaria, cuerpo("")), "ATENDIDAS");
        enviar(post("/factura/generar"), secretaria, "{\"citaId\":99999}").andExpect(status().isNotFound());
        assertFalse(despachoDetalles.findById(entregado.getId()).orElseThrow().getFacturado());
    }

    @Test
    void pagosParcialesHastaPagada() throws Exception {
        long id = generar(secretaria, "");
        conflicto(pagar(id, "147350.01"), "supera el saldo");
        pagar(id, "0").andExpect(status().isBadRequest());
        pagar(id, "-5").andExpect(status().isBadRequest());
        pagar(id, "1.005").andExpect(status().isBadRequest());
        pagar(id, "100000").andExpect(status().isOk()).andExpect(jsonPath("$.data.estado").value("PAGO_PARCIAL"))
                .andExpect(jsonPath("$.data.saldo").value(47350));
        pagar(id, "47350").andExpect(status().isOk()).andExpect(jsonPath("$.data.estado").value("PAGADA"))
                .andExpect(jsonPath("$.data.pagado").value(147350)).andExpect(jsonPath("$.data.pagos.length()").value(2));
        conflicto(pagar(id, "1"), "no admite pagos");
        consultar("/factura/" + id + "/pagos").andExpect(jsonPath("$[0].medioPago").value("EFECTIVO"))
                .andExpect(jsonPath("$[0].usuario").isNotEmpty());
        consultar("/factura/" + id + "/historial-estados").andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].estadoAnterior").isEmpty())
                .andExpect(jsonPath("$[2].estadoNuevo").value("PAGADA"));
    }

    @Test
    void anularConservaTodoYPermiteVolverAFacturar() throws Exception {
        long id = generar(secretaria, "");
        pagar(id, "1000").andExpect(status().isOk());
        enviar(post("/factura/" + id + "/anular"), admin, "{}").andExpect(status().isBadRequest());
        enviar(post("/factura/" + id + "/anular"), admin, "{\"motivo\":\"Error\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("pagos por $ 1.000,00")))
                .andExpect(jsonPath("$.data.estado").value("ANULADA"))
                .andExpect(jsonPath("$.data.motivoAnulacion").value("Error"))
                .andExpect(jsonPath("$.data.usuarioAnulacion").isNotEmpty())
                .andExpect(jsonPath("$.data.detalles.length()").value(3));
        conflicto(enviar(post("/factura/" + id + "/anular"), admin, "{\"motivo\":\"x\"}"), "ya está anulada");
        conflicto(pagar(id, "1"), "no admite pagos");
        assertEquals(1, pagos.count());
        assertFalse(despachoDetalles.findById(entregado.getId()).orElseThrow().getFacturado());
        consultar("/factura/citas-pendientes").andExpect(jsonPath("$.length()").value(1));
        long nueva = generar(secretaria, "");
        assertNotEquals(id, nueva);
        consultar("/factura/listar").andExpect(jsonPath("$.length()").value(2));
        consultar("/factura/listar?estado=ANULADA").andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].detalles").doesNotExist());
        consultar("/factura/listar?numero=000002").andExpect(jsonPath("$[0].id").value(nueva));
        consultar("/factura/cliente/" + cita.getCliente().getId()).andExpect(jsonPath("$.length()").value(2));
        consultar("/factura/mascota/" + cita.getMascota().getMascotaId()).andExpect(jsonPath("$.length()").value(2));
        consultar("/factura/listar?desde=2026-02-01&hasta=2026-01-01", 400);
    }

    @Test
    void generacionesYPagosSimultaneos() throws Exception {
        assertEquals(1, simultaneas(5, () -> enviar(post("/factura/generar"), secretaria, cuerpo(""))));
        assertEquals(1, facturas.count());
        long id = facturas.findAll().getFirst().getId();
        // 147.350 admite dos pagos de 60.000.
        assertEquals(2, simultaneas(5, () -> pagar(id, "60000")));
        consultar("/factura/" + id).andExpect(jsonPath("$.saldo").value(27350));
        assertEquals(2, pagos.count());
    }

    @Test
    void pdfConTodosLosDatosYMarcaDeAnulada() throws Exception {
        long id = generar(admin, descuento("SERVICIO-" + consulta.getId(), "10", "Convenio"));
        pagar(id, "40000").andExpect(status().isOk());
        String texto = pdf(id);
        // 45.000 + 8.550 + 70.000 + 15.000 + 2.850 = 141.400
        for (String t : new String[]{"FACTURA FV-000001", "Clínica Prueba", "Juan Pérez", "CC 10", "Luna", "Ana Rojas",
                "General", "Consulta", "Vacuna", "Amoxicilina", "$ 5.000,00 (10 %)", "IVA 19 %", "$ 11.400,00", "$ 141.400,00",
                "PAGO PARCIAL", "EFECTIVO", "$ 40.000,00", "$ 101.400,00"}) {
            assertTrue(texto.contains(t), t + " en " + texto);
        }
        enviar(post("/factura/" + id + "/anular"), admin, "{\"motivo\":\"Error\"}").andExpect(status().isOk());
        assertTrue(pdf(id).contains("Anulación"));
    }

    @ParameterizedTest
    @CsvSource({"MEDICO,GET,/factura/listar", "DESPACHO_MEDICO,GET,/factura/1", "MEDICO,POST,/factura/generar",
            "SECRETARIA,POST,/factura/1/anular", "DESPACHO_MEDICO,POST,/factura/1/pagos", "MEDICO,GET,/factura/1/pdf"})
    void permisos(String rol, String metodo, String ruta) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(metodo), ruta).header("Authorization", token(rol))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------ apoyo

    private int simultaneas(int veces, Callable<ResultActions> accion) throws Exception {
        List<Callable<Integer>> tareas = new ArrayList<>();
        for (int i = 0; i < veces; i++) {
            tareas.add(() -> accion.call().andReturn().getResponse().getStatus());
        }
        int exitosas = 0;
        try (var hilos = Executors.newFixedThreadPool(veces)) {
            for (Future<Integer> f : hilos.invokeAll(tareas)) {
                int estado = f.get();
                assertTrue(estado == 200 || estado == 409, "estado " + estado);
                exitosas += estado == 200 ? 1 : 0;
            }
        }
        return exitosas;
    }

    private long generar(String token, String descuentos) throws Exception {
        return json.readTree(enviar(post("/factura/generar"), token, cuerpo(descuentos)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8)).get("data").get("id").asLong();
    }

    private String cuerpo(String descuentos) {
        return "{\"citaId\":" + cita.getId() + ",\"observacion\":\"ok\",\"descuentos\":[" + descuentos + "]}";
    }

    private static String descuento(String ref, String porcentaje, String motivo) {
        return "{\"itemRef\":\"" + ref + "\",\"porcentaje\":" + porcentaje + ",\"motivo\":\"" + motivo + "\"}";
    }

    private ResultActions pagar(long id, String valor) throws Exception {
        return enviar(post("/factura/" + id + "/pagos"), secretaria, "{\"valor\":" + valor
                + ",\"medioPago\":\"EFECTIVO\",\"referencia\":null}");
    }

    private ResultActions consultar(String ruta) throws Exception {
        return consultar(ruta, 200);
    }

    private ResultActions consultar(String ruta, int estado) throws Exception {
        return mvc.perform(get(ruta).header("Authorization", secretaria)).andExpect(status().is(estado));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, String cuerpo) throws Exception {
        return mvc.perform(peticion.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    private void conflicto(ResultActions r, String mensaje) throws Exception {
        r.andExpect(status().isConflict()).andExpect(jsonPath("$.message").value(containsString(mensaje)));
    }

    private String pdf(long id) throws Exception {
        byte[] contenido = mvc.perform(get("/factura/" + id + "/pdf").header("Authorization", secretaria))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"))
                .andReturn().getResponse().getContentAsByteArray();
        try (PdfReader lector = new PdfReader(contenido)) {
            return new PdfTextExtractor(lector).getTextFromPage(1).replaceAll("\\s+", " ");
        }
    }

    private DespachoDetalle entregar(int cantidad, EstadoPreparacion estado) {
        Despacho d = new Despacho();
        d.setFormula(formula);
        d.setNumero("DP-" + despachos.count());
        d.setUsuario(usuarioDespacho);
        d.setEstado(estado);
        d.setFechaInicio(LocalDateTime.now());
        d.setFechaConfirmacion(LocalDateTime.now());
        d = despachos.save(d);
        LoteMedicamento l = new LoteMedicamento();
        l.setMedicamento(amoxicilina);
        l.setNumeroLote("L" + lotes.count());
        l.setPrecioCompra(BigDecimal.ONE);
        l.setCantidadInicial(100);
        l.setCantidadDisponible(50);
        l.setFechaCompra(LocalDate.now().minusDays(9));
        l.setFechaIngreso(LocalDate.now().minusDays(9));
        l.setFechaVencimiento(LocalDate.now().plusDays(99));
        DespachoDetalle x = new DespachoDetalle();
        x.setDespacho(d);
        x.setFormulaDetalle(formulado);
        x.setMedicamento(amoxicilina);
        x.setLote(lotes.save(l));
        x.setCantidadEntregada(cantidad);
        x.setPrecioVentaUnitario(BigDecimal.valueOf(1500));
        x.setPorcentajeIva(BigDecimal.valueOf(19));
        return despachoDetalles.save(x);
    }

    private FormulaMedica formula(AtencionMedica a) {
        FormulaMedica f = new FormulaMedica();
        f.setNumero("FM-000001");
        f.setCita(cita);
        f.setAtencion(a);
        f.setMedico(cita.getMedico());
        f.setMascota(cita.getMascota());
        f.setCliente(cita.getCliente());
        f.setEstadoFormula(EstadoFormula.CONFIRMADA);
        f.setEstadoDespacho(EstadoDespacho.DESPACHO_PARCIAL);
        f.setFechaGeneracion(LocalDateTime.now());
        return formulas.save(f);
    }

    private FormulaMedicaDetalle formulado(int cantidad) {
        FormulaMedicaDetalle d = new FormulaMedicaDetalle();
        d.setFormula(formula);
        d.setMedicamento(amoxicilina);
        d.setDosis(BigDecimal.ONE);
        d.setUnidadDosis("tableta");
        d.setFrecuenciaHoras(8);
        d.setDuracionDias(7);
        d.setViaAdministracion(ViaAdministracion.ORAL);
        d.setCantidadPrescrita(cantidad);
        d.setCantidadDespachada(10);
        return formulaDetalles.save(d);
    }

    private Impuesto impuesto(String nombre, int porcentaje) {
        Impuesto i = new Impuesto();
        i.setNombre(nombre);
        i.setPorcentaje(BigDecimal.valueOf(porcentaje));
        return impuestos.save(i);
    }

    private Servicio servicio(String codigo, String nombre) {
        Servicio s = new Servicio();
        s.setCodigo(codigo);
        s.setNombre(nombre);
        s.setTipoServicio(TipoServicio.CONSULTA);
        return servicios.save(s);
    }

    private Tarifa tarifa(Servicio s, int valor, Impuesto i) {
        Tarifa t = new Tarifa();
        t.setServicio(s);
        t.setValor(BigDecimal.valueOf(valor));
        t.setImpuesto(i);
        t.setVigenteDesde(LocalDate.now());
        return tarifas.save(t);
    }

    private void prestar(AtencionMedica a, Servicio s, int cantidad) {
        AtencionServicio x = new AtencionServicio();
        x.setAtencion(a);
        x.setServicio(s);
        x.setCantidad(cantidad);
        atencionServicios.save(x);
    }

    private AtencionMedica atencion(Cita c) {
        HistoriaMedica h = new HistoriaMedica();
        h.setMascota(c.getMascota());
        AtencionMedica a = new AtencionMedica();
        a.setCita(c);
        a.setHistoriaMedica(historias.save(h));
        a.setMedico(c.getMedico());
        a.setMascota(c.getMascota());
        a.setFechaInicio(LocalDateTime.now());
        a.setFechaFin(LocalDateTime.now());
        a.setFinalizada(true);
        return atenciones.save(a);
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
