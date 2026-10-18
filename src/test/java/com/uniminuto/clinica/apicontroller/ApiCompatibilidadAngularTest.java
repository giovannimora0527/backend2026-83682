package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de extremo a extremo de la API con los contratos que usa el frontend Angular.
 * Se ejecutan contra H2 en memoria (perfil test); nunca contra la base MySQL real.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApiCompatibilidadAngularTest {
    private static final String ANGULAR = "http://localhost:4200";

    @Autowired private WebApplicationContext context;
    @Autowired private EntityManager em;
    @Autowired private ClienteRepository clientes;
    @Autowired private MascotaRepository mascotas;
    @Autowired private RazaRepository razas;
    @Autowired private VeterinarioRepository veterinarios;
    @Autowired private CitaRepository citas;
    @Autowired private MedicamentoRepository medicamentos;
    @Autowired private UsuarioRepository usuarios;

    private MockMvc mvc;
    private Cliente cliente;
    private Raza raza;
    private Mascota conCita;
    private Mascota sinRegistros;
    private Veterinario vet;
    private Cita cita;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        cliente = clientes.saveAndFlush(cliente("900001"));
        raza = razas.saveAndFlush(raza());
        conCita = mascotas.saveAndFlush(mascota("Luna", raza, cliente));
        sinRegistros = mascotas.saveAndFlush(mascota("Toby", raza, cliente));
        Especializacion esp = new Especializacion();
        esp.setId(1); esp.setNombre("General"); esp.setCodigoEspecializacion("GEN");
        em.persist(esp); em.flush();
        vet = veterinarios.saveAndFlush(veterinario(esp));
        cita = new Cita();
        cita.setCliente(cliente); cita.setMascota(conCita); cita.setVeterinario(vet);
        cita.setFechaHora(LocalDateTime.of(2026, 10, 1, 9, 0)); cita.setEstado(EstadoCita.PROGRAMADA);
        cita.setMotivoConsulta("Control");
        cita = citas.saveAndFlush(cita);
        Medicamento med = new Medicamento(); med.setNombre("Paracetamol");
        medicamentos.saveAndFlush(med);
    }

    @Test
    void corsPermiteAngularEnPreflightYRespuestasYRechazaOtrosOrigenes() throws Exception {
        mvc.perform(options("/citas").header("Origin", ANGULAR)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR));
        mvc.perform(options("/mascota/eliminar/1").header("Origin", ANGULAR)
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("DELETE")));
        mvc.perform(get("/cliente/listar").header("Origin", ANGULAR))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR));
        mvc.perform(options("/citas").header("Origin", "http://sitio-no-permitido.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rutaInexistenteDevuelve404JsonConCabeceraCors() throws Exception {
        mvc.perform(get("/no-existe").header("Origin", ANGULAR))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR));
        mvc.perform(get("/citas/999999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        mvc.perform(get("/citas").param("veterinarioId", "abc")).andExpect(status().isBadRequest());
        mvc.perform(get("/mascota/buscar-by-cliente")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("clienteId")));
        mvc.perform(post("/citas/1")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void usuarioListaSinExponerHashYCreaActualizaSegunElParcial() throws Exception {
        mvc.perform(get("/usuario/test")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value(200));
        mvc.perform(post("/usuario/guardar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana\",\"password\":\"secreta\",\"rol\":\"ADMIN\",\"email\":\"ana@clinica.com\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/usuario/guardar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana\",\"password\":\"x\",\"rol\":\"ADMIN\",\"email\":\"otra@clinica.com\"}"))
                .andExpect(status().isConflict());
        Usuario guardado = usuarios.findAll().stream().filter(u -> "ana".equals(u.getUsername())).findFirst().orElseThrow();
        String hash = guardado.getPasswordHash();
        assertEquals(32, hash.length());

        for (String ruta : new String[]{"/usuario/listar", "/usuario/listar-ordenado"}) {
            mvc.perform(get(ruta))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value("ana"))
                    .andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                    .andExpect(jsonPath("$[0].password_hash").doesNotExist())
                    .andExpect(content().string(not(containsString(hash))));
        }

        mvc.perform(post("/usuario/actualizar").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + guardado.getId() + ",\"username\":\"ana\",\"rol\":\"VET\",\"email\":\"ana@clinica.com\"}"))
                .andExpect(status().isOk());
        em.flush(); em.clear();
        Usuario actualizado = usuarios.findById(guardado.getId()).orElseThrow();
        assertEquals("VET", actualizado.getRol());
        assertEquals(hash, actualizado.getPasswordHash(), "Sin contraseña nueva se conserva el hash");
    }

    @Test
    void eliminarMascotaProtegeRegistrosClinicosYEliminaLasLibres() throws Exception {
        mvc.perform(delete("/mascota/eliminar/{id}", conCita.getMascotaId()).header("Origin", ANGULAR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("1 cita(s)")))
                .andExpect(header().string("Access-Control-Allow-Origin", ANGULAR));
        assertTrue(mascotas.existsById(conCita.getMascotaId()));

        mvc.perform(delete("/mascota/eliminar/{id}", sinRegistros.getMascotaId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Mascota eliminada correctamente"));
        assertFalse(mascotas.existsById(sinRegistros.getMascotaId()));

        mvc.perform(delete("/mascota/eliminar/{id}", 999999)).andExpect(status().isNotFound());
    }

    @Test
    void formulaConElPayloadDeAngularSeGuardaSoloEnColumnasExistentesYSeLeeIgual() throws Exception {
        String payload = """
                {"mascotaId": %d, "veterinarioId": %d, "fechaEmision": "2026-10-05",
                 "medicamento": "Paracetamol", "dosis": 2.5, "frecuencia": "Cada 8 horas",
                 "duracionDias": 7, "indicaciones": "Con alimento"}
                """.formatted(conCita.getMascotaId(), vet.getVeterinarioId());
        String creado = mvc.perform(post("/formulas-medicas").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mascotaId").value(conCita.getMascotaId()))
                .andExpect(jsonPath("$.veterinarioId").value(vet.getVeterinarioId()))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(creado.replaceAll(".*\"formulaMedicaId\":(\\d+).*", "$1"));
        em.flush(); em.clear();

        mvc.perform(get("/formulas-medicas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaEmision").value("2026-10-05"))
                .andExpect(jsonPath("$.medicamento").value("Paracetamol"))
                .andExpect(jsonPath("$.dosis").value(2.5))
                .andExpect(jsonPath("$.frecuencia").value("Cada 8 horas"))
                .andExpect(jsonPath("$.duracionDias").value(7))
                .andExpect(jsonPath("$.indicaciones").value("Con alimento"))
                .andExpect(jsonPath("$.clienteId").value(cliente.getClienteId()));
        mvc.perform(get("/formulas-medicas").param("fecha", "2026-10-05").param("veterinarioId", String.valueOf(vet.getVeterinarioId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].formulaMedicaId").value(id))
                // Campos que pinta la tabla de Angular (nombreCliente, dosis · frecuencia).
                .andExpect(jsonPath("$.content[0].nombreCliente").value("Juan Perez"))
                .andExpect(jsonPath("$.content[0].dosis").value(2.5))
                .andExpect(jsonPath("$.content[0].frecuencia").value("Cada 8 horas"));
        mvc.perform(get("/formula-medica/listar-ordenado").param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].formulaMedicaId").value(id));

        String sinCita = payload.replace("\"mascotaId\": " + conCita.getMascotaId(), "\"mascotaId\": " + sinRegistros.getMascotaId());
        mvc.perform(post("/formulas-medicas").contentType(MediaType.APPLICATION_JSON).content(sinCita))
                .andExpect(status().isConflict());
    }

    @Test
    void historiasOrdenanPorTodasLasColumnasCalculanEstadoYFiltranAnotacionesDelDetalle() throws Exception {
        Veterinario otroVet = new Veterinario();
        otroVet.setTipoDocumento("CC"); otroVet.setNumeroDocumento("800002"); otroVet.setNombres("Bruno");
        otroVet.setApellidos("Zapata"); otroVet.setRegistroProfesional("REG-901"); otroVet.setEspecializacion(vet.getEspecializacion());
        otroVet = veterinarios.saveAndFlush(otroVet);
        long historiaToby = crearHistoria(sinRegistros, vet, "2026-10-02T10:00:00", "Alergia");
        long historiaLuna = crearHistoria(conCita, otroVet, "2026-10-03T10:00:00", "Vomito");
        HistoriaClinica sinNotas = new HistoriaClinica();
        sinNotas.setMascota(conCita); sinNotas.setFechaHora(LocalDateTime.of(2026, 10, 1, 8, 0));
        em.persist(sinNotas); em.flush();

        // Orden por columnas (antes: 400 en mascota, cliente, veterinario y motivo).
        mvc.perform(get("/historias-clinicas").param("sort", "mascotaId").param("direction", "ASC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].mascotaNombre").value("Luna"))
                .andExpect(jsonPath("$.content[2].mascotaNombre").value("Toby"));
        mvc.perform(get("/historias-clinicas").param("sort", "clienteId").param("direction", "DESC"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get("/historias-clinicas").param("sort", "motivoConsulta").param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].motivoConsulta").value("Alergia"))
                .andExpect(jsonPath("$.content[1].motivoConsulta").value("Vomito"));
        mvc.perform(get("/historias-clinicas").param("sort", "veterinarioId").param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].veterinarioNombre").value("Bruno Zapata"))
                .andExpect(jsonPath("$.content[1].veterinarioNombre").value("Ana Lopez"));
        // El orden en memoria respeta la paginación.
        mvc.perform(get("/historias-clinicas").param("sort", "motivoConsulta").param("direction", "ASC")
                        .param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].historiaClinicaId").value(historiaLuna))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.page").value(1));
        mvc.perform(get("/historias-clinicas").param("sort", "noExiste")).andExpect(status().isBadRequest());

        // Estado calculado (no es una columna).
        mvc.perform(get("/historias-clinicas").param("mascotaId", String.valueOf(conCita.getMascotaId()))
                        .param("sort", "fechaHora").param("direction", "ASC"))
                .andExpect(jsonPath("$.content[0].estado").value("Sin anotaciones"))
                .andExpect(jsonPath("$.content[1].estado").value("Con anotaciones"));
        mvc.perform(get("/historias-clinicas/{id}", historiaToby))
                .andExpect(jsonPath("$.historia.estado").value("Con anotaciones"));

        // Filtros y orden de las anotaciones del detalle; sin filtros el endpoint responde igual que antes.
        mvc.perform(post("/anotaciones").contentType(MediaType.APPLICATION_JSON).content("""
                        {"historiaClinicaId": %d, "veterinarioId": %d, "fechaHora": "2026-10-05T09:00:00", "descripcion": "Control"}
                        """.formatted(historiaLuna, vet.getVeterinarioId())))
                .andExpect(status().isCreated());
        mvc.perform(get("/historias-clinicas/{id}/anotaciones", historiaLuna))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].descripcion").value("Control"));
        mvc.perform(get("/historias-clinicas/{id}/anotaciones", historiaLuna).param("veterinarioId", String.valueOf(vet.getVeterinarioId())))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].descripcion").value("Control"));
        mvc.perform(get("/historias-clinicas/{id}/anotaciones", historiaLuna).param("fecha", "2026-10-03"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/historias-clinicas/{id}/anotaciones", historiaLuna).param("sort", "fechaHora").param("direction", "ASC"))
                .andExpect(jsonPath("$.content[1].descripcion").value("Control"));
        mvc.perform(get("/historias-clinicas/{id}/anotaciones", 999999).param("fecha", "2026-10-03"))
                .andExpect(status().isNotFound());
    }

    private long crearHistoria(Mascota mascota, Veterinario veterinario, String fechaHora, String motivo) throws Exception {
        String json = mvc.perform(post("/historias-clinicas").contentType(MediaType.APPLICATION_JSON).content("""
                        {"clienteId": %d, "mascotaId": %d, "veterinarioId": %d, "fechaHora": "%s",
                         "motivoConsulta": "%s", "diagnostico": "En estudio"}
                        """.formatted(cliente.getClienteId(), mascota.getMascotaId(), veterinario.getVeterinarioId(), fechaHora, motivo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return Long.parseLong(json.replaceAll(".*\"historiaClinicaId\":(\\d+).*", "$1"));
    }

    @Test
    void historiaYAnotacionConservanObservacionYDiagnostico() throws Exception {
        String historia = """
                {"clienteId": %d, "mascotaId": %d, "veterinarioId": %d, "fechaHora": "2026-10-02T10:00:00",
                 "motivoConsulta": "Vomito", "diagnostico": "Gastritis", "observaciones": "Dieta blanda"}
                """.formatted(cliente.getClienteId(), conCita.getMascotaId(), vet.getVeterinarioId());
        String creada = mvc.perform(post("/historias-clinicas").contentType(MediaType.APPLICATION_JSON).content(historia))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long historiaId = Long.parseLong(creada.replaceAll(".*\"historiaClinicaId\":(\\d+).*", "$1"));

        String nota = """
                {"historiaClinicaId": %d, "veterinarioId": %d, "fechaHora": "2026-10-03T08:00:00",
                 "descripcion": "Control", "observacion": "Mejor apetito", "diagnosticoEvolucion": "Favorable"}
                """.formatted(historiaId, vet.getVeterinarioId());
        String creadaNota = mvc.perform(post("/anotaciones").contentType(MediaType.APPLICATION_JSON).content(nota))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long notaId = Long.parseLong(creadaNota.replaceAll(".*\"anotacionId\":(\\d+).*", "$1"));
        em.flush(); em.clear();

        mvc.perform(get("/anotaciones/{id}", notaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Control"))
                .andExpect(jsonPath("$.observacion").value("Mejor apetito"))
                .andExpect(jsonPath("$.diagnosticoEvolucion").value("Favorable"));
        // La historia muestra sus datos originales aunque exista una anotación posterior.
        mvc.perform(get("/historias-clinicas/{id}", historiaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historia.motivoConsulta").value("Vomito"))
                .andExpect(jsonPath("$.historia.diagnostico").value("Gastritis"));
        mvc.perform(get("/historias-clinicas/{id}/anotaciones", historiaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/anotaciones").param("historiaClinicaId", String.valueOf(historiaId)).param("fecha", "2026-10-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void columnasOrdenablesDeAnotacionesYFormulasResponden200() throws Exception {
        for (String campo : new String[]{"fechaHora", "historiaClinicaId", "veterinarioId"})
            for (String dir : new String[]{"ASC", "DESC"})
                mvc.perform(get("/anotaciones").param("sort", campo).param("direction", dir))
                        .andExpect(status().isOk());
        for (String campo : new String[]{"fechaCreacionRegistro", "mascotaId", "veterinarioId", "medicamento"})
            for (String dir : new String[]{"ASC", "DESC"})
                mvc.perform(get("/formulas-medicas").param("sort", campo).param("direction", dir))
                        .andExpect(status().isOk());
        mvc.perform(get("/anotaciones").param("sort", "noExiste")).andExpect(status().isBadRequest());
    }

    @Test
    void rutasDelParcialSiguenDisponibles() throws Exception {
        mvc.perform(get("/cliente/all")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/cliente/cliente-documento").param("numeroDocumento", "900001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.clienteId").value(cliente.getClienteId()));
        mvc.perform(get("/cliente/cliente-documento").param("numeroDocumento", "000")).andExpect(status().isNotFound());
        mvc.perform(get("/cliente/{id}/mascotas", cliente.getClienteId())).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/mascota/buscar").param("nombre", "luna"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mascotaId").value(conCita.getMascotaId()));
        mvc.perform(get("/mascota/listar-ordenado").param("ascendente", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nombreMascota").value("Toby"));
        mvc.perform(get("/medico/all")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/medico/buscar").param("tipoDocumento", "CC").param("numeroDocumento", "800001"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.veterinarioId").value(vet.getVeterinarioId()));
        mvc.perform(get("/medico/buscarPorRegistroProfesional").param("registroProfesional", "REG-900"))
                .andExpect(status().isOk());
        mvc.perform(get("/cita/filtrar-by-fechas").param("fechaInicio", "2026-09-30T00:00:00")
                        .param("fechaFinal", "2026-10-02T00:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].citaId").value(cita.getCitaId()));
        mvc.perform(get("/veterinaria/test")).andExpect(status().isOk());
        // Columnas ordenables de la tabla de citas en Angular (PDF 2.1).
        for (String columna : new String[]{"fecha", "hora", "clienteId", "mascotaId", "veterinarioId", "estado"}) {
            for (String direccion : new String[]{"ASC", "DESC"}) {
                mvc.perform(get("/citas").param("sort", columna).param("direction", direccion))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].citaId").value(cita.getCitaId()));
            }
        }
        // Hora ordena por la hora del día, no por la fecha completa.
        Cita tarde = new Cita();
        tarde.setCliente(cliente); tarde.setMascota(sinRegistros); tarde.setVeterinario(vet);
        tarde.setFechaHora(LocalDateTime.of(2025, 1, 1, 16, 45)); tarde.setEstado(EstadoCita.PENDIENTE);
        tarde.setMotivoConsulta("Control");
        tarde = citas.saveAndFlush(tarde);
        mvc.perform(get("/citas").param("sort", "hora").param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].hora").value("16:45:00"))
                .andExpect(jsonPath("$.content[1].hora").value("09:00:00"));
        mvc.perform(get("/citas").param("sort", "hora").param("direction", "ASC"))
                .andExpect(jsonPath("$.content[0].citaId").value(cita.getCitaId()));
        citas.delete(tarde);
        citas.flush();
        mvc.perform(get("/citas").param("estado", "PROGRAMADA").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5));
    }

    private Cliente cliente(String documento) {
        Cliente c = new Cliente();
        c.setTipoDocumento("CC"); c.setNumeroDocumento(documento); c.setNombres("Juan"); c.setApellidos("Perez");
        c.setFechaNacimiento(LocalDate.of(1990, 1, 1)); c.setActivo(true);
        return c;
    }

    private Raza raza() {
        Raza r = new Raza();
        r.setNombre("Criollo"); r.setEspecie("Canino"); r.setFechaCreacion(LocalDateTime.of(2026, 1, 1, 0, 0));
        return r;
    }

    private Mascota mascota(String nombre, Raza raza, Cliente cliente) {
        Mascota m = new Mascota();
        m.setNombreMascota(nombre); m.setEdad(3); m.setRaza(raza); m.setCliente(cliente);
        m.setFechaRegistro(LocalDateTime.of(2026, 1, 1, 0, 0));
        return m;
    }

    private Veterinario veterinario(Especializacion esp) {
        Veterinario v = new Veterinario();
        v.setTipoDocumento("CC"); v.setNumeroDocumento("800001"); v.setNombres("Ana"); v.setApellidos("Lopez");
        v.setRegistroProfesional("REG-900"); v.setEspecializacion(esp);
        return v;
    }
}
