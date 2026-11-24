package com.uniminuto.clinica;

import com.uniminuto.clinica.entity.Impuesto;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Inventario y carga masiva (R1). */
class InventarioTests extends PruebaIntegracion {

    private static final String ENCABEZADO = "codigo,nombre,tipo_producto,descripcion,presentacion,unidad_medida,"
            + "requiere_invima,registro_invima,precio_compra,precio_venta,porcentaje_iva,stock_minimo,numero_lote,"
            + "cantidad,fecha_compra,fecha_vencimiento,fecha_proximo_pedido";

    @Autowired MedicamentoRepository medicamentos;
    @Autowired LoteMedicamentoRepository lotes;
    @Autowired MovimientoInventarioRepository movimientos;
    @Autowired CargaInventarioRepository cargas;
    @Autowired CargaInventarioFilaRepository filas;
    @Autowired ImpuestoRepository impuestos;
    @Autowired ObjectMapper json;

    private String admin;

    @BeforeEach
    void preparar() {
        admin = token("ADMINISTRADOR");
        for (int porcentaje : new int[]{0, 19}) {
            Impuesto impuesto = new Impuesto();
            impuesto.setNombre("IVA " + porcentaje);
            impuesto.setPorcentaje(BigDecimal.valueOf(porcentaje));
            impuestos.save(impuesto);
        }
    }

    @Override
    protected void limpiarDatos() {
        movimientos.deleteAllInBatch();
        filas.deleteAllInBatch();
        cargas.deleteAllInBatch();
        lotes.deleteAllInBatch();
        medicamentos.deleteAllInBatch();
        impuestos.deleteAllInBatch();
    }

    // ------------------------------------------------------------ productos

    @Test
    void crearConLoteInicialRegistraMovimiento() throws Exception {
        crear(producto("amx00500", "Amoxicilina", "MEDICAMENTO", "INV-1", lote("L1", 100, 900, 400)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codigo").value("AMX00500"))
                .andExpect(jsonPath("$.data.cantidadDisponible").value(100))
                .andExpect(jsonPath("$.data.porcentajeIva").value(19))
                .andExpect(jsonPath("$.data.estadosStock[0]").value("DISPONIBLE"))
                .andExpect(jsonPath("$.data.lotes[0].vencido").value(false));
        MovimientoInventario m = movimientos.findAll().getFirst();
        assertEquals(100, m.getSaldoLoteResultante());
        assertEquals(100, m.getSaldoProductoResultante());
    }

    @Test
    void validaCodigoInvimaYDuplicados() throws Exception {
        crear(producto("AMX0050", "A", "MEDICAMENTO", "I", "null")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0].campo").value("codigo"));
        crear(producto("AMX00500", "A", "MEDICAMENTO", "", "null")).andExpect(status().isBadRequest());
        crear(producto("JER00001", "Jeringa", "INSUMO", "", "null")).andExpect(status().isOk());
        crear(producto("jer00001", "Otra", "INSUMO", "", "null")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0].campo").value("codigo"));
        crear(producto("JER00002", " JERINGA ", "INSUMO", "", "null")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.detalles[0].campo").value("nombre"));
    }

    @Test
    void loteInvalidoNoDejaProductoCreado() throws Exception {
        crear(producto("AMX00500", "A", "MEDICAMENTO", "I", lote("L1", 10, 100, -1)))
                .andExpect(status().is4xxClientError());
        crear(producto("AMX00500", "A", "MEDICAMENTO", "I", """
                {"numeroLote":"L1","precioCompra":10,"cantidad":5,"fechaCompra":"%s"}""".formatted(LocalDate.now())))
                .andExpect(status().isBadRequest());
        assertEquals(0, medicamentos.count());
    }

    @Test
    void editarNoCambiaCantidades() throws Exception {
        long id = idCreado(producto("AMX00500", "A", "MEDICAMENTO", "I", lote("L1", 100, 900, 400)));
        enviar(put("/inventario/productos/" + id), producto("AMX00500", "B", "MEDICAMENTO", "I", "null"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.nombre").value("B"))
                .andExpect(jsonPath("$.data.cantidadDisponible").value(100));
        enviar(put("/inventario/productos/" + id),
                producto("AMX00500", "B", "MEDICAMENTO", "I", lote("L2", 5, 1, 400)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void preciosPromedioYUltimoYLoteRepetido() throws Exception {
        long id = idCreado(producto("AMX00500", "A", "MEDICAMENTO", "I", lote("L1", 100, 900, 400)));
        enviar(post("/inventario/productos/" + id + "/lotes"), lote("L2", 300, 1300, 500))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cantidadDisponible").value(400))
                .andExpect(jsonPath("$.data.precioCompraUltimo").value(1300))
                .andExpect(jsonPath("$.data.precioCompraPromedio").value(1200));
        enviar(post("/inventario/productos/" + id + "/lotes"), lote("l2", 1, 1, 500))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detalles[0].campo").value("numeroLote"));
    }

    @Test
    void ajustesNuncaDejanNegativosYQuedanEnElHistorial() throws Exception {
        long id = idCreado(producto("AMX00500", "A", "MEDICAMENTO", "I", lote("L1", 100, 900, 400)));
        long loteId = lotes.findAll().getFirst().getId();
        ajustar(id, loteId, "AJUSTE_NEGATIVO", 101).andExpect(status().isConflict());
        ajustar(id, loteId, "ENTRADA_COMPRA", 1).andExpect(status().isBadRequest());
        ajustar(id, loteId, "AJUSTE_NEGATIVO", 95).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cantidadDisponible").value(5))
                .andExpect(jsonPath("$.data.estadosStock[0]").value("BAJO"));
        ajustar(id, loteId, "AJUSTE_POSITIVO", 200).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lotes[0].cantidadInicial").value(205));
        mvc.perform(get("/inventario/productos/" + id + "/movimientos").header("Authorization", admin))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].tipo").value("AJUSTE_POSITIVO"))
                .andExpect(jsonPath("$[0].saldo").value(205))
                .andExpect(jsonPath("$[0].motivo").value("Conteo"));
        mvc.perform(get("/inventario/movimientos?tipo=AJUSTE_NEGATIVO").header("Authorization", admin))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/inventario/movimientos?desde=2026-02-01&hasta=2026-01-01").header("Authorization", admin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ajustesSimultaneosNoPierdenUnidades() throws Exception {
        long id = idCreado(producto("AMX00500", "A", "MEDICAMENTO", "I", lote("L1", 10, 900, 400)));
        long loteId = lotes.findAll().getFirst().getId();
        List<Callable<Integer>> tareas = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            tareas.add(() -> ajustar(id, loteId, "AJUSTE_NEGATIVO", 2).andReturn().getResponse().getStatus());
        }
        int exitosos = 0;
        try (var hilos = Executors.newFixedThreadPool(8)) {
            for (Future<Integer> f : hilos.invokeAll(tareas)) {
                exitosos += f.get() == 200 ? 1 : 0;
            }
        }
        assertEquals(10 - exitosos * 2, lotes.findById(loteId).orElseThrow().getCantidadDisponible());
        assertEquals(1 + exitosos, movimientos.count());
        assertTrue(exitosos >= 1 && exitosos <= 5);
    }

    @Test
    void estadosAlertasYDisponibilidad() throws Exception {
        long id = idCreado(producto("AMX00500", "Amoxicilína", "MEDICAMENTO", "I", lote("L1", 100, 900, 10)));
        idCreado(producto("JER00001", "Jeringa", "INSUMO", "", "null"));
        Medicamento m = medicamentos.findByCodigo("AMX00500").orElseThrow();
        LoteMedicamento vencido = new LoteMedicamento();
        vencido.setMedicamento(m);
        vencido.setNumeroLote("V");
        vencido.setPrecioCompra(BigDecimal.TEN);
        vencido.setCantidadInicial(7);
        vencido.setCantidadDisponible(7);
        vencido.setFechaCompra(LocalDate.now().minusDays(50));
        vencido.setFechaIngreso(LocalDate.now().minusDays(50));
        vencido.setFechaVencimiento(LocalDate.now().minusDays(1));
        lotes.save(vencido);
        mvc.perform(get("/inventario/alertas").header("Authorization", token("DESPACHO_MEDICO")))
                .andExpect(jsonPath("$.agotados").value(1)).andExpect(jsonPath("$.proximosVencer").value(1))
                .andExpect(jsonPath("$.vencidos").value(1)).andExpect(jsonPath("$.bajoStock").value(0));
        mvc.perform(get("/inventario/disponibilidad/" + id).header("Authorization", token("MEDICO")))
                .andExpect(jsonPath("$.cantidadDisponibleVigente").value(100))
                .andExpect(jsonPath("$.cantidadVencida").value(7))
                .andExpect(jsonPath("$.lotesVigentes.length()").value(1));
        mvc.perform(get("/inventario/productos/" + id + "/lotes?soloVigentes=true").header("Authorization", admin))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/inventario/productos?texto=amoxicilina").header("Authorization", admin))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].lotes").doesNotExist());
        mvc.perform(get("/inventario/productos?estadoStock=AGOTADO").header("Authorization", admin))
                .andExpect(jsonPath("$[0].codigo").value("JER00001"));
        mvc.perform(get("/inventario/productos?estadoStock=XX").header("Authorization", admin))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({"DESPACHO_MEDICO,GET,/inventario/productos,200", "DESPACHO_MEDICO,GET,/inventario/movimientos,200",
            "DESPACHO_MEDICO,POST,/inventario/productos,403", "DESPACHO_MEDICO,POST,/inventario/productos/1/lotes,403",
            "DESPACHO_MEDICO,POST,/inventario/productos/1/ajustes,403", "MEDICO,GET,/inventario/productos,200",
            "DESPACHO_MEDICO,PATCH,/inventario/productos/1/estado,403", "MEDICO,GET,/inventario/movimientos,403",
            "MEDICO,GET,/inventario/alertas,403", "SECRETARIA,GET,/inventario/productos,403",
            "DESPACHO_MEDICO,GET,/inventario/carga/plantilla,403", "MEDICO,POST,/inventario/carga/1/confirmar,403"})
    void permisos(String rol, String metodo, String ruta, int esperado) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(metodo), ruta).header("Authorization", token(rol))
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().is(esperado));
    }

    // ------------------------------------------------------------ carga masiva

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "csv"})
    void evidenciaDe60FilasImporta55YReporta5Errores(String formato) throws Exception {
        byte[] archivo = Files.readAllBytes(Path.of("db/evidencias/carga_inventario_60." + formato));
        JsonNode previa = leer(subir("carga_inventario_60." + formato, archivo).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PREVISUALIZADA"))
                .andExpect(jsonPath("$.totalFilas").value(60))
                .andExpect(jsonPath("$.filasValidas").value(55))
                .andExpect(jsonPath("$.filasConError").value(5)));
        List<Integer> conError = new ArrayList<>();
        previa.get("filas").forEach(f -> {
            if (!f.get("valida").asBoolean()) {
                conError.add(f.get("fila").asInt());
            }
        });
        assertEquals(List.of(11, 21, 32, 41, 51), conError);
        assertEquals(0, medicamentos.count(), "La vista previa no escribe en inventario");

        String ruta = "/inventario/carga/" + previa.get("cargaId").asLong();
        mvc.perform(post(ruta + "/confirmar").header("Authorization", admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.filasImportadas").value(55))
                .andExpect(jsonPath("$.data.estado").value("CONFIRMADA_CON_ERRORES"))
                .andExpect(jsonPath("$.data.usuarioConfirmacion").isNotEmpty());
        assertEquals(55, medicamentos.count());
        assertEquals(55, lotes.count());
        assertEquals(55, movimientos.count());
        mvc.perform(post(ruta + "/confirmar").header("Authorization", admin)).andExpect(status().isConflict());
        mvc.perform(post(ruta + "/descartar").header("Authorization", admin)).andExpect(status().isConflict());
        assertEquals(55, movimientos.count());

        String csv = mvc.perform(get(ruta + "/errores/archivo?formato=csv").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment; filename=\"errores_carga_")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(csv.contains("11,precio_venta,-100,Debe ser mayor que 0"));
        assertTrue(csv.contains("21,codigo,EVI0020,"));
        mvc.perform(get("/inventario/carga/historial").header("Authorization", admin))
                .andExpect(jsonPath("$[0].filasImportadas").value(55))
                .andExpect(jsonPath("$[0].filas").doesNotExist());
        // Volver a subir el mismo archivo no duplica: todos los lotes ya existen.
        subir("otra." + formato, archivo).andExpect(jsonPath("$.filasValidas").value(0));
    }

    @Test
    void encabezadosEquivocadosListanFaltantesYSobrantes() throws Exception {
        subir("a.csv", ENCABEZADO.replace("codigo,", "cod,") + "\n1").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Encabezados incorrectos. Faltan: codigo. Sobran: cod."));
        subir("a.csv", ENCABEZADO.replace("codigo,nombre", "nombre,codigo") + "\n1").andExpect(status().isBadRequest());
        subir("a.csv", ENCABEZADO.toUpperCase() + "\n" + fila("AAA00001", "Uno", "L1", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.filasValidas").value(1));
    }

    @Test
    void archivosInvalidos() throws Exception {
        subir("a.txt", "x").andExpect(status().isBadRequest());
        subir("a.xlsx", "no es excel").andExpect(status().isBadRequest());
        subir("a.csv", ENCABEZADO).andExpect(status().isBadRequest());
        subir("a.csv", (ENCABEZADO + "\nñ").getBytes(StandardCharsets.ISO_8859_1)).andExpect(status().isBadRequest());
        mvc.perform(multipart("/inventario/carga/previsualizar").header("Authorization", admin))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/inventario/carga/plantilla?formato=pdf").header("Authorization", admin))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/inventario/carga/99999").header("Authorization", admin)).andExpect(status().isNotFound());
    }

    @Test
    void puntoYComaFechaDiaMesYAdvertencia() throws Exception {
        String f = fila("AAA00001", "Uno", "L1", "10").replace("2026-01-10", "10/01/2026").replace(",1500,", ",50,");
        subir("a.csv", (ENCABEZADO + "\n" + f).replace(',', ';')).andExpect(status().isOk())
                .andExpect(jsonPath("$.filas[0].valida").value(true))
                .andExpect(jsonPath("$.filas[0].accion").value("NUEVO_PRODUCTO"))
                .andExpect(jsonPath("$.fecha").value(org.hamcrest.Matchers.matchesPattern("[0-9-]{10}T[0-9]{2}:[0-9]{2}:[0-9]{2}")))
                .andExpect(jsonPath("$.filas[0].advertencias[0].campo").value("precio_venta"));
    }

    @Test
    void duplicadosContraLaBaseYDentroDelArchivo() throws Exception {
        idCreado(producto("AMX00500", "Amoxicilina", "MEDICAMENTO", "I", lote("L1", 10, 900, 400)));
        subir("a.csv", String.join("\n", ENCABEZADO,
                fila("AMX00500", "Amoxicilina", "L1", "5"),
                fila("AMX00500", "Otro nombre", "L2", "5"),
                fila("BBB00001", "amoxicilina", "L3", "5"),
                fila("AMX00500", "AMOXICILINA", "L4", "5"),
                fila("CCC00001", "Nuevo", "L1", "5"),
                fila("CCC00001", "Nuevo", "L2", "5"),
                fila("CCC00001", "Nuevo", "l2", "5"),
                fila("DDD00001", "Decimal", "L1", "1.5")))
                .andExpect(jsonPath("$.filas[0].errores[0].campo").value("numero_lote"))
                .andExpect(jsonPath("$.filas[1].errores[0].campo").value("codigo"))
                .andExpect(jsonPath("$.filas[2].errores[0].campo").value("nombre"))
                .andExpect(jsonPath("$.filas[3].accion").value("NUEVO_LOTE"))
                .andExpect(jsonPath("$.filas[4].accion").value("NUEVO_PRODUCTO"))
                .andExpect(jsonPath("$.filas[5].accion").value("NUEVO_LOTE"))
                .andExpect(jsonPath("$.filas[6].errores[0].campo").value("numero_lote"))
                .andExpect(jsonPath("$.filas[7].errores[0].campo").value("cantidad"))
                .andExpect(jsonPath("$.filasValidas").value(3));
    }

    @Test
    void unaFilaQueFallaAlImportarNoAfectaALasDemas() throws Exception {
        JsonNode previa = leer(subir("a.csv", String.join("\n", ENCABEZADO, fila("AAA00001", "Uno", "L1", "10"),
                fila("BBB00001", "Dos", "L1", "10"), fila("CCC00001", "Tres", "L1", "10"))));
        // Entre la vista previa y la confirmacion alguien crea un producto con el mismo nombre.
        idCreado(producto("ZZZ00009", "Dos", "INSUMO", "", "null"));
        String ruta = "/inventario/carga/" + previa.get("cargaId").asLong();
        mvc.perform(post(ruta + "/confirmar").header("Authorization", admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.filasImportadas").value(2))
                .andExpect(jsonPath("$.data.estado").value("CONFIRMADA_CON_ERRORES"))
                .andExpect(jsonPath("$.data.filas[1].importada").value(false))
                .andExpect(jsonPath("$.data.filas[1].errorImportacion").isNotEmpty());
        assertTrue(medicamentos.findByCodigo("AAA00001").isPresent());
        assertTrue(medicamentos.findByCodigo("CCC00001").isPresent());
        assertTrue(medicamentos.findByCodigo("BBB00001").isEmpty());
    }

    @Test
    void descartarImpideConfirmarYSinFilasValidasTampoco() throws Exception {
        long id = leer(subir("a.csv", ENCABEZADO + "\n" + fila("AAA00001", "Uno", "L1", "10"))).get("cargaId").asLong();
        mvc.perform(post("/inventario/carga/" + id + "/descartar").header("Authorization", admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.estado").value("DESCARTADA"));
        mvc.perform(post("/inventario/carga/" + id + "/confirmar").header("Authorization", admin))
                .andExpect(status().isConflict());
        long mala = leer(subir("a.csv", ENCABEZADO + "\n" + fila("X", "Uno", "L1", "10"))).get("cargaId").asLong();
        mvc.perform(post("/inventario/carga/" + mala + "/confirmar").header("Authorization", admin))
                .andExpect(status().isConflict());
        assertEquals(0, medicamentos.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"xlsx", "csv"})
    void laPlantillaSePuedeVolverASubir(String formato) throws Exception {
        byte[] plantilla = mvc.perform(get("/inventario/carga/plantilla?formato=" + formato)
                        .header("Authorization", admin)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        subir("plantilla." + formato, plantilla).andExpect(status().isOk())
                .andExpect(jsonPath("$.filasValidas").value(2));
    }

    // ------------------------------------------------------------ apoyo

    private ResultActions subir(String nombre, String contenido) throws Exception {
        return subir(nombre, contenido.getBytes(StandardCharsets.UTF_8));
    }

    private ResultActions subir(String nombre, byte[] contenido) throws Exception {
        return mvc.perform(multipart("/inventario/carga/previsualizar")
                .file(new MockMultipartFile("archivo", nombre, null, contenido)).header("Authorization", admin));
    }

    private JsonNode leer(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String cuerpo) throws Exception {
        return mvc.perform(peticion.header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    private ResultActions crear(String cuerpo) throws Exception {
        return enviar(post("/inventario/productos"), cuerpo);
    }

    private long idCreado(String cuerpo) throws Exception {
        return leer(crear(cuerpo).andExpect(status().isOk())).get("data").get("id").asLong();
    }

    private ResultActions ajustar(long id, long loteId, String tipo, int cantidad) throws Exception {
        return enviar(post("/inventario/productos/" + id + "/ajustes"), """
                {"loteId":%d,"tipo":"%s","cantidad":%d,"motivo":"Conteo"}""".formatted(loteId, tipo, cantidad));
    }

    private static String producto(String codigo, String nombre, String tipo, String invima, String lote) {
        return """
                {"codigo":"%s","nombre":"%s","tipoProducto":"%s","registroInvima":"%s","unidadMedida":"tableta",
                 "precioVenta":1500,"porcentajeIva":19,"stockMinimo":20,"loteInicial":%s}"""
                .formatted(codigo, nombre, tipo, invima, lote);
    }

    private static String lote(String numero, int cantidad, int precio, int diasVence) {
        return """
                {"numeroLote":"%s","precioCompra":%d,"cantidad":%d,"fechaCompra":"%s","fechaVencimiento":"%s"}"""
                .formatted(numero, precio, cantidad, LocalDate.now().minusDays(5), LocalDate.now().plusDays(diasVence));
    }

    private static String fila(String codigo, String nombre, String lote, String cantidad) {
        return codigo + "," + nombre + ",MEDICAMENTO,,,tableta,SI,INV-1,900,1500,19,5," + lote + "," + cantidad
                + ",2026-01-10,2035-01-01,";
    }
}
