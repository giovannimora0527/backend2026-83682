package com.uniminuto.clinica;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.entity.enums.*;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import tools.jackson.databind.JsonNode;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Reportes (R4, 5.9): los totales deben cuadrar con facturas, pagos, lotes y movimientos. */
class ReportesTests extends PruebaIntegracion {

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

    private static final String HOY = "desde=" + LocalDate.now() + "&hasta=" + LocalDate.now();

    @Test
    void catalogoTieneLos21Reportes() throws Exception {
        mvc.perform(get("/reporte/catalogo").header("Authorization", admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(21)).andExpect(jsonPath("$[?(@.codigo=='kardex')].grupo").value("Inventario"))
                .andExpect(jsonPath("$[?(@.codigo=='ingresos-periodo')].filtros[2].opciones[0]").value("DIA"));
    }

    /** Factura 1: 147.350, pagos 100.000 + 47.350. Factura 2: anulada con pago de 1.000; la reemplaza la 3, sin pagos. */
    @Test
    void ingresosCuadranConFacturasYPagos() throws Exception {
        long primera = generar(admin, descuento("SERVICIO-" + consulta.getId(), "10", "Convenio"));
        // 45.000 + 8.550 + 70.000 + 15.000 + 2.850 = 141.400
        pagar(primera, "1000").andExpect(status().isOk());
        enviar(post("/factura/" + primera + "/anular"), admin, "{\"motivo\":\"x\"}").andExpect(status().isOk());
        long segunda = generar(admin, "");
        pagar(segunda, "73675").andExpect(status().isOk());

        for (String agrupacion : new String[]{"DIA", "MES", "ANIO"}) {
            JsonNode r = reporte("ingresos-periodo?" + HOY + "&agrupacion=" + agrupacion);
            assertEquals(1, r.get("filas").size());
            assertEquals(agrupacion, r.get("filtros").get("agrupacion").asString());
            igual(147350, r.get("totales").get("facturado"));
            igual(74675, r.get("totales").get("recaudado"));
            igual(1, r.get("totales").get("facturas"));
        }
        assertEquals(LocalDate.now().toString(), reporte("ingresos-periodo?" + HOY + "&agrupacion=DIA").get("filas").get(0)
                .get("periodo").asString());
        JsonNode consultas = reporte("ingresos-consultas?" + HOY).get("totales");
        JsonNode medicamentosVenta = reporte("ingresos-medicamentos?" + HOY).get("totales");
        // Ambos servicios son CONSULTA: 59.500 + 70.000; medicamentos 17.850.
        igual(129500, consultas.get("facturado"));
        igual(17850, medicamentosVenta.get("facturado"));
        igual(10, medicamentosVenta.get("cantidad"));
        igual(74675, new BigDecimal(consultas.get("recaudado").asString()).add(new BigDecimal(medicamentosVenta.get("recaudado").asString())));
        for (String codigo : new String[]{"ingresos-especialidad", "ingresos-veterinario"}) {
            JsonNode r = reporte(codigo + "?" + HOY);
            assertEquals(1, r.get("filas").size());
            igual(147350, r.get("totales").get("facturado"));
            igual(74675, r.get("totales").get("recaudado"));
        }
        JsonNode impuestosRs = reporte("impuestos?" + HOY);
        igual(12350, impuestosRs.get("totales").get("iva"));
        igual(135000, impuestosRs.get("totales").get("base"));
        assertEquals(2, impuestosRs.get("filas").size());
        assertEquals(0, reporte("descuentos?" + HOY).get("filas").size(), "El descuento era de la anulada");

        JsonNode todas = reporte("facturas?" + HOY);
        assertEquals(2, todas.get("filas").size());
        igual(147350, todas.get("totales").get("total"));
        igual(73675, todas.get("totales").get("saldo"));
        assertEquals(1, reporte("facturas?" + HOY + "&estado=PAGO_PARCIAL").get("filas").size());
        igual(141400, reporte("facturas?" + HOY + "&estado=ANULADA").get("totales").get("total"));
        assertEquals(0, reporte("facturas?" + HOY + "&estado=PAGADA").get("filas").size());
        String manana = "desde=" + LocalDate.now().plusDays(1) + "&hasta=" + LocalDate.now().plusDays(2);
        assertEquals(0, reporte("ingresos-periodo?" + manana).get("filas").size());
    }

    @Test
    void descuentosDetallados() throws Exception {
        generar(admin, descuento("SERVICIO-" + consulta.getId(), "10", "Convenio"));
        JsonNode r = reporte("descuentos?" + HOY);
        igual(5000, r.get("totales").get("descuento"));
        assertEquals("Convenio", r.get("filas").get(0).get("motivo").asString());
    }

    /** Un lote vigente de 50 (costo 1) creado por la base de pruebas, mas los de este test. */
    @Test
    void inventarioCuadraConLotes() throws Exception {
        enviar(post("/inventario/productos"), admin, producto("JER00001", "Jeringa", "INSUMO", "null")).andExpect(status().isOk());
        enviar(post("/inventario/productos"), admin, producto("GAS00001", "Gasa", "INSUMO", lote(8, 100, 10))).andExpect(status().isOk());
        LoteMedicamento vencido = new LoteMedicamento();
        vencido.setMedicamento(amoxicilina);
        vencido.setNumeroLote("VENC");
        vencido.setPrecioCompra(BigDecimal.valueOf(20));
        vencido.setCantidadInicial(5);
        vencido.setCantidadDisponible(5);
        vencido.setFechaCompra(LocalDate.now().minusDays(50));
        vencido.setFechaIngreso(LocalDate.now().minusDays(50));
        vencido.setFechaVencimiento(LocalDate.now().minusDays(1));
        lotes.save(vencido);
        JsonNode existencias = reporte("existencias");
        assertEquals(3, existencias.get("filas").size());
        igual(63, existencias.get("totales").get("cantidad"));
        igual(58, existencias.get("totales").get("vigente"));
        igual(5, existencias.get("totales").get("vencida"));
        JsonNode valor = reporte("valor-inventario");
        // Costo: 50x1 + 5x20 + 8x100; venta: 55x1500 + 8x2000.
        igual(950, valor.get("totales").get("valorCosto"));
        igual(98500, valor.get("totales").get("valorVenta"));
        assertEquals("Jeringa", reporte("agotados").get("filas").get(0).get("nombre").asString());
        assertEquals("Gasa", reporte("stock-minimo").get("filas").get(0).get("nombre").asString());
        JsonNode proximos = reporte("proximos-vencer");
        assertEquals(1, proximos.get("filas").size());
        assertEquals(30, proximos.get("filtros").get("dias").asInt());
        assertEquals(0, reporte("proximos-vencer?dias=5").get("filas").size());
        assertEquals(2, reporte("proximos-vencer?dias=200").get("filas").size());
        JsonNode vencidos = reporte("vencidos");
        igual(100, vencidos.get("totales").get("valorCosto"));
        assertEquals(-1, vencidos.get("filas").get(0).get("dias").asInt());
    }

    @Test
    void movimientosYKardexCuadranConExistencias() throws Exception {
        JsonNode p = json.readTree(enviar(post("/inventario/productos"), admin, producto("GAS00001", "Gasa", "INSUMO", lote(80, 100, 90)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8)).get("data");
        long id = p.get("id").asLong();
        enviar(post("/inventario/productos/" + id + "/ajustes"), admin, "{\"loteId\":" + p.get("lotes").get(0).get("id").asLong()
                + ",\"tipo\":\"AJUSTE_NEGATIVO\",\"cantidad\":30,\"motivo\":\"Conteo\"}").andExpect(status().isOk());
        JsonNode kardex = reporte("kardex?" + HOY + "&medicamentoId=" + id);
        assertEquals(3, kardex.get("filas").size());
        assertEquals("SALDO INICIAL", kardex.get("filas").get(0).get("tipo").asString());
        igual(80, kardex.get("totales").get("entrada"));
        igual(30, kardex.get("totales").get("salida"));
        igual(50, kardex.get("totales").get("saldo"));
        igual(3000, kardex.get("totales").get("valorSalida"));
        igual(50, kardex.get("filas").get(2).get("saldo"));
        JsonNode futuro = reporte("kardex?desde=" + LocalDate.now().plusDays(1) + "&hasta=" + LocalDate.now().plusDays(2) + "&medicamentoId=" + id);
        igual(50, futuro.get("filas").get(0).get("saldo"));
        assertEquals(2, reporte("movimientos?" + HOY).get("filas").size());
        assertEquals(1, reporte("movimientos?" + HOY + "&tipo=AJUSTE_NEGATIVO").get("filas").size());
        assertEquals(0, reporte("movimientos?" + HOY + "&medicamentoId=" + amoxicilina.getId()).get("filas").size());
        mvc.perform(get("/reporte/kardex?" + HOY).header("Authorization", admin)).andExpect(status().isBadRequest());
        mvc.perform(get("/reporte/kardex?" + HOY + "&medicamentoId=99999").header("Authorization", admin)).andExpect(status().isNotFound());
    }

    @Test
    void gestionMedicaCuadraConCitasYAtenciones() throws Exception {
        Cita cancelada = new Cita();
        cancelada.setMascota(cita.getMascota());
        cancelada.setCliente(cita.getCliente());
        cancelada.setMedico(cita.getMedico());
        cancelada.setFechaInicio(cita.getFechaInicio());
        cancelada.setFechaFin(cita.getFechaFin());
        cancelada.setEstado(EstadoCita.CANCELADA);
        cancelada.setMotivo("x");
        citas.save(cancelada);
        JsonNode periodo = reporte("citas-periodo?" + HOY + "&agrupacion=DIA");
        igual(2, periodo.get("totales").get("total"));
        igual(1, periodo.get("totales").get("atendidas"));
        igual(1, periodo.get("totales").get("canceladas"));
        igual(2, reporte("citas-estado?" + HOY).get("totales").get("cantidad"));
        assertEquals("CANCELADA", reporte("citas-estado?" + HOY + "&estado=CANCELADA").get("filas").get(0).get("estado").asString());
        JsonNode veterinario = reporte("consultas-veterinario?" + HOY + "&medicoId=" + cita.getMedico().getId());
        igual(1, veterinario.get("totales").get("consultas"));
        igual(1, veterinario.get("totales").get("canceladas"));
        assertEquals("Ana Rojas", veterinario.get("filas").get(0).get("nombre").asString());
        assertEquals(0, reporte("consultas-veterinario?" + HOY + "&medicoId=99999").get("filas").size());
        igual(1, reporte("consultas-especialidad?" + HOY).get("totales").get("consultas"));
        JsonNode atendidas = reporte("mascotas-atendidas?" + HOY);
        igual(1, atendidas.get("totales").get("mascotas"));
        assertEquals("Luna", atendidas.get("filas").get(0).get("mascota").asString());
    }

    @Test
    void todosSeExportanConEncabezadosYTotales() throws Exception {
        generar(admin, "");
        JsonNode catalogo = json.readTree(mvc.perform(get("/reporte/catalogo").header("Authorization", admin))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        for (JsonNode r : catalogo) {
            String codigo = r.get("codigo").asString();
            String filtros = "?" + HOY + "&medicamentoId=" + amoxicilina.getId();
            JsonNode previa = reporte(codigo + filtros);
            assertTrue(previa.get("columnas").size() > 1, codigo);
            byte[] excel = exportar(codigo + "/exportar" + filtros + "&formato=xlsx", ".xlsx");
            try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
                Sheet hoja = libro.getSheetAt(0);
                assertEquals("Clínica Prueba", hoja.getRow(0).getCell(0).getStringCellValue());
                assertEquals(previa.get("titulo").asString(), hoja.getRow(1).getCell(0).getStringCellValue());
                assertEquals("TOTAL", hoja.getRow(hoja.getLastRowNum()).getCell(0).getStringCellValue(), codigo);
            }
            byte[] pdf = exportar(codigo + "/exportar" + filtros + "&formato=pdf", ".pdf");
            try (PdfReader lector = new PdfReader(pdf)) {
                String texto = new PdfTextExtractor(lector).getTextFromPage(1).replaceAll("\\s+", " ");
                assertTrue(texto.contains(previa.get("titulo").asString()) && texto.contains("TOTAL")
                        && texto.contains("Generado") && texto.contains("Página 1"), codigo + ": " + texto);
            }
        }
        byte[] excel = exportar("facturas/exportar?" + HOY + "&formato=xlsx", ".xlsx");
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet hoja = libro.getSheetAt(0);
            var total = hoja.getRow(hoja.getLastRowNum()).getCell(9);
            assertEquals(CellType.NUMERIC, total.getCellType());
            assertEquals(147350, total.getNumericCellValue());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ingresos-periodo", "ingresos-periodo?desde=2026-02-01&hasta=2026-01-01",
            "ingresos-periodo?desde=hoy&hasta=2026-01-01", "ingresos-periodo?desde=2026-01-01&hasta=2026-02-01&agrupacion=SEMANA",
            "facturas?desde=2026-01-01&hasta=2026-02-01&estado=XX", "proximos-vencer?dias=0", "proximos-vencer?dias=abc",
            "facturas/exportar?desde=2026-01-01&hasta=2026-02-01&formato=doc", "facturas/exportar?desde=2026-01-01&hasta=2026-02-01"})
    void filtrosInvalidos(String ruta) throws Exception {
        mvc.perform(get("/reporte/" + ruta).header("Authorization", admin)).andExpect(status().isBadRequest());
    }

    @Test
    void permisosEInexistente() throws Exception {
        mvc.perform(get("/reporte/no-existe").header("Authorization", admin)).andExpect(status().isNotFound());
        for (String rol : new String[]{"SECRETARIA", "MEDICO", "DESPACHO_MEDICO"}) {
            mvc.perform(get("/reporte/catalogo").header("Authorization", token(rol))).andExpect(status().isForbidden());
            mvc.perform(get("/reporte/facturas/exportar?formato=pdf&" + HOY).header("Authorization", token(rol)))
                    .andExpect(status().isForbidden());
        }
    }

    private JsonNode reporte(String ruta) throws Exception {
        return json.readTree(mvc.perform(get("/reporte/" + ruta).header("Authorization", admin)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private byte[] exportar(String ruta, String extension) throws Exception {
        return mvc.perform(get("/reporte/" + ruta).header("Authorization", admin)).andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(extension)))
                .andReturn().getResponse().getContentAsByteArray();
    }

    private static void igual(double esperado, JsonNode valor) {
        igual(esperado, new BigDecimal(valor.asString()));
    }

    private static void igual(double esperado, BigDecimal valor) {
        assertEquals(0, BigDecimal.valueOf(esperado).compareTo(valor), "esperado " + esperado + ", fue " + valor);
    }

    private static String producto(String codigo, String nombre, String tipo, String lote) {
        return """
                {"codigo":"%s","nombre":"%s","tipoProducto":"%s","precioVenta":2000,"stockMinimo":20,"loteInicial":%s}"""
                .formatted(codigo, nombre, tipo, lote);
    }

    private static String lote(int cantidad, int costo, int diasVence) {
        return """
                {"numeroLote":"X","precioCompra":%d,"cantidad":%d,"fechaCompra":"%s","fechaVencimiento":"%s"}"""
                .formatted(costo, cantidad, LocalDate.now().minusDays(5), LocalDate.now().plusDays(diasVence));
    }

    // ------------------------------------------------------------ apoyo

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

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String token, String cuerpo) throws Exception {
        return mvc.perform(peticion.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(cuerpo));
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
