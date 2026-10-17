package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.PageResponse;
import com.uniminuto.clinica.models.FormulaResumenResponse;
import com.uniminuto.clinica.models.FormulaRequest;
import com.uniminuto.clinica.models.FormulaResponse;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FormulaMedicaListingIntegrationTest {
    @Autowired private FormulaMedicaServiceImpl service;
    @Autowired private ClienteRepository clientes;
    @Autowired private MascotaRepository mascotas;
    @Autowired private RazaRepository razas;
    @Autowired private EntityManager entityManager;
    @Autowired private VeterinarioRepository veterinarios;
    @Autowired private CitaRepository citas;
    @Autowired private MedicamentoRepository medicamentos;
    @Autowired private FormulaMedicaRepository formulas;

    @Test
    void filtraPorClienteYOrdenaPorElAliasPublicoMascotaId() {
        Cliente clienteA = clientes.saveAndFlush(cliente("100001"));
        Cliente clienteB = clientes.saveAndFlush(cliente("100002"));
        Raza raza = razas.saveAndFlush(raza());
        Mascota mascotaA1 = mascotas.saveAndFlush(mascota("Luna", raza, clienteA));
        Mascota mascotaA2 = mascotas.saveAndFlush(mascota("Milo", raza, clienteA));
        Mascota mascotaB = mascotas.saveAndFlush(mascota("Nina", raza, clienteB));
        Especializacion especializacion = especializacion();
        entityManager.persist(especializacion);
        entityManager.flush();
        Veterinario veterinario = veterinarios.saveAndFlush(veterinario(especializacion));
        Medicamento medicamento = medicamentos.saveAndFlush(medicamento());

        // Save in reverse mascota order so the response proves that sort is applied.
        guardarFormula(cita(mascotaA2, clienteA, veterinario), medicamento, LocalDate.of(2026, 10, 14));
        guardarFormula(cita(mascotaA1, clienteA, veterinario), medicamento, LocalDate.of(2026, 10, 15));
        guardarFormula(cita(mascotaB, clienteB, veterinario), medicamento, LocalDate.of(2026, 10, 16));

        PageResponse<FormulaResumenResponse> page = service.listar(0, 20, "mascotaId", "ASC",
                clienteA.getClienteId(), null, null, null, null);

        assertEquals(2, page.totalElements());
        assertEquals(2, page.content().size());
        assertEquals(mascotaA1.getMascotaId(), page.content().get(0).mascotaId());
        assertEquals(mascotaA2.getMascotaId(), page.content().get(1).mascotaId());
        assertTrue(page.content().stream().allMatch(f -> clienteA.getClienteId().equals(f.clienteId())));
    }

    @Test
    void persisteRecuperaActualizaYListaFormulasConCitaHistoriaOAambas() {
        Cliente cliente = clientes.saveAndFlush(cliente("100011"));
        Raza raza = razas.saveAndFlush(raza());
        Mascota mascota = mascotas.saveAndFlush(mascota("Luna", raza, cliente));
        Especializacion especializacion = especializacion();
        entityManager.persist(especializacion);
        entityManager.flush();
        Veterinario veterinario = veterinarios.saveAndFlush(veterinario(especializacion));
        Medicamento medicamento = medicamentos.saveAndFlush(medicamento());
        Cita cita = cita(mascota, cliente, veterinario);
        HistoriaClinica historia = new HistoriaClinica();
        historia.setMascota(mascota);
        historia.setFechaHora(LocalDateTime.of(2026, 10, 2, 10, 0));
        historia = entityManager.merge(historia);
        entityManager.flush();

        LocalDate fechaEmision = LocalDate.of(2026, 10, 16);
        FormulaResponse soloCita = service.crear(request(mascota, historia, veterinario, medicamento,
                cita, fechaEmision, false));
        FormulaResponse soloHistoria = service.crear(request(mascota, historia, veterinario, medicamento,
                null, fechaEmision, true));
        FormulaResponse ambas = service.crear(request(mascota, historia, veterinario, medicamento,
                cita, fechaEmision, true));

        assertNull(soloCita.historiaClinicaId());
        assertEquals(mascota.getMascotaId(), soloHistoria.mascotaId());
        assertEquals(historia.getHistoriaClinicaId(), soloHistoria.historiaClinicaId());
        assertEquals(historia.getHistoriaClinicaId(), ambas.historiaClinicaId());

        entityManager.flush();
        entityManager.clear();
        FormulaResponse citaRecuperada = service.consultar(soloCita.formulaMedicaId());
        FormulaResponse historiaRecuperada = service.consultar(soloHistoria.formulaMedicaId());
        FormulaResponse ambasRecuperada = service.consultar(ambas.formulaMedicaId());
        assertEquals(fechaEmision, citaRecuperada.fechaEmision());
        assertEquals(mascota.getMascotaId(), historiaRecuperada.mascotaId());
        assertEquals(cliente.getClienteId(), historiaRecuperada.clienteId());
        assertEquals(veterinario.getVeterinarioId(), historiaRecuperada.veterinarioId());
        assertEquals(fechaEmision, ambasRecuperada.fechaEmision());

        PageResponse<FormulaResumenResponse> listado = service.listar(0, 20, null, "DESC",
                cliente.getClienteId(), null, veterinario.getVeterinarioId(), null, fechaEmision);
        assertEquals(3, listado.totalElements());
        assertTrue(listado.content().stream().allMatch(f -> cliente.getClienteId().equals(f.clienteId())));
        assertTrue(listado.content().stream().allMatch(f -> mascota.getMascotaId().equals(f.mascotaId())));

        LocalDate fechaActualizada = fechaEmision.plusDays(1);
        FormulaResponse actualizada = service.actualizar(soloHistoria.formulaMedicaId(),
                request(mascota, historia, veterinario, medicamento, cita, fechaActualizada, true));
        assertEquals(historia.getHistoriaClinicaId(), actualizada.historiaClinicaId());
        assertEquals(fechaActualizada, actualizada.fechaEmision());

        PageResponse<FormulaResumenResponse> actualizadaEnListado = service.listar(0, 20, null, "DESC",
                cliente.getClienteId(), null, veterinario.getVeterinarioId(), null, fechaActualizada);
        assertEquals(1, actualizadaEnListado.totalElements());
        assertEquals(soloHistoria.formulaMedicaId(), actualizadaEnListado.content().get(0).formulaMedicaId());
    }

    @Test
    void rechazaCitaEHistoriaConMascotasDistintas() {
        Cliente cliente = clientes.saveAndFlush(cliente("100012"));
        Cliente otroCliente = clientes.saveAndFlush(cliente("100013"));
        Raza raza = razas.saveAndFlush(raza());
        Mascota mascotaCita = mascotas.saveAndFlush(mascota("Luna", raza, cliente));
        Mascota mascotaHistoria = mascotas.saveAndFlush(mascota("Milo", raza, otroCliente));
        Especializacion especializacion = especializacion();
        entityManager.persist(especializacion);
        entityManager.flush();
        Veterinario veterinario = veterinarios.saveAndFlush(veterinario(especializacion));
        Medicamento medicamento = medicamentos.saveAndFlush(medicamento());
        Cita cita = cita(mascotaCita, cliente, veterinario);
        HistoriaClinica historia = new HistoriaClinica();
        historia.setMascota(mascotaHistoria);
        historia.setFechaHora(LocalDateTime.of(2026, 10, 2, 10, 0));
        historia = entityManager.merge(historia);
        entityManager.flush();

        HistoriaClinica incompatible = historia;
        assertThrows(BadRequestException.class, () -> service.crear(request(mascotaCita, incompatible,
                veterinario, medicamento, cita, LocalDate.of(2026, 10, 16), true)));
        assertEquals(0, formulas.count());
    }

    private FormulaRequest request(Mascota mascota, HistoriaClinica historia, Veterinario veterinario,
            Medicamento medicamento, Cita cita, LocalDate fechaEmision, boolean incluirHistoria) {
        return new FormulaRequest(mascota.getMascotaId(), incluirHistoria ? historia.getHistoriaClinicaId() : null,
                veterinario.getVeterinarioId(), fechaEmision, medicamento.getNombre(), BigDecimal.ONE,
                "Cada 8 horas", 5, "Con alimento", cita == null ? null : cita.getCitaId(), medicamento.getId());
    }

    private Cliente cliente(String document) {
        Cliente cliente = new Cliente();
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento(document);
        cliente.setNombres("Cliente");
        cliente.setApellidos(document);
        cliente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        cliente.setActivo(true);
        return cliente;
    }

    private Raza raza() {
        Raza raza = new Raza();
        raza.setNombre("Canino");
        raza.setEspecie("Perro");
        raza.setFechaCreacion(LocalDateTime.of(2026, 1, 1, 0, 0));
        return raza;
    }

    private Mascota mascota(String nombre, Raza raza, Cliente cliente) {
        Mascota mascota = new Mascota();
        mascota.setNombreMascota(nombre);
        mascota.setEdad(3);
        mascota.setRaza(raza);
        mascota.setCliente(cliente);
        mascota.setFechaRegistro(LocalDateTime.of(2026, 1, 1, 0, 0));
        return mascota;
    }

    private Especializacion especializacion() {
        Especializacion especializacion = new Especializacion();
        especializacion.setId(1);
        especializacion.setNombre("General");
        especializacion.setCodigoEspecializacion("GEN");
        return especializacion;
    }

    private Veterinario veterinario(Especializacion especializacion) {
        Veterinario veterinario = new Veterinario();
        veterinario.setTipoDocumento("CC");
        veterinario.setNumeroDocumento("200001");
        veterinario.setNombres("Médico");
        veterinario.setApellidos("Prueba");
        veterinario.setRegistroProfesional("REG-1");
        veterinario.setEspecializacion(especializacion);
        return veterinario;
    }

    private Medicamento medicamento() {
        Medicamento medicamento = new Medicamento();
        medicamento.setNombre("Medicamento de prueba");
        return medicamento;
    }

    private Cita cita(Mascota mascota, Cliente cliente, Veterinario veterinario) {
        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setCliente(cliente);
        cita.setVeterinario(veterinario);
        cita.setFechaHora(LocalDateTime.of(2026, 10, 1, 9, 0));
        cita.setEstado(EstadoCita.ATENDIDA);
        return citas.saveAndFlush(cita);
    }

    private void guardarFormula(Cita cita, Medicamento medicamento, LocalDate emision) {
        FormulaMedica formula = new FormulaMedica();
        formula.setCita(cita);
        formula.setMedicamentoEntity(medicamento);
        formula.setDosisTexto(BigDecimal.ONE.toPlainString());
        formula.setFechaCreacion(emision.atStartOfDay());
        formula.setIndicaciones("Frecuencia: Cada 8 horas\nDuración en días: 5");
        formulas.saveAndFlush(formula);
    }
}
