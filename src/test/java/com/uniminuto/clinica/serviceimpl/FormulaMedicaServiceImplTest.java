package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRequest;
import com.uniminuto.clinica.models.FormulaResponse;
import com.uniminuto.clinica.models.PageResponse;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormulaMedicaServiceImplTest {
    @Mock FormulaMedicaRepository formulas;
    @Mock HistoriaClinicaRepository historias;
    @Mock VeterinarioRepository veterinarios;
    @Mock CitaRepository citas;
    @Mock MedicamentoRepository medicamentos;
    @InjectMocks FormulaMedicaServiceImpl service;

    @Test
    void desdeHistoriaClinicaResuelveLaCitaDeLaMascotaPorqueLaTablaSoloGuardaCitaId() {
        LocalDate emision = LocalDate.of(2026, 10, 16);
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        HistoriaClinica historia = historia(21L, mascota, vet, emision.minusDays(1));
        Cita cita = cita(51L, mascota, vet, emision.minusDays(1));
        Medicamento medicamento = medicamento(31L, "Antibiótico");
        when(veterinarios.findById(11L)).thenReturn(Optional.of(vet));
        when(historias.findById(21L)).thenReturn(Optional.of(historia));
        when(historias.findFirstByMascota_MascotaIdOrderByFechaHoraAsc(4)).thenReturn(Optional.of(historia));
        when(citas.findFirstByMascota_MascotaIdAndVeterinario_VeterinarioIdAndEstadoNotOrderByFechaHoraDesc(4, 11L, EstadoCita.CANCELADA))
                .thenReturn(Optional.of(cita));
        when(medicamentos.findFirstByNombreIgnoreCase("Antibiótico")).thenReturn(Optional.of(medicamento));
        when(formulas.save(any())).thenAnswer(call -> call.getArgument(0));

        FormulaResponse response = service.crear(request(4, 21L, 11L, emision, null));

        assertEquals(emision, response.fechaEmision());
        assertEquals(21L, response.historiaClinicaId());
        assertEquals(11L, response.veterinarioId());
        verify(formulas).save(argThat(f -> f.getCita() == cita
                && f.getIndicaciones().contains("Fecha de emisión: " + emision)));
    }

    @Test
    void fechaDeEmisionIgualAlRegistroNoAgregaLineaYSeDerivaDeLaFechaDeRegistro() {
        LocalDate hoy = LocalDate.now();
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cita = cita(51L, mascota, vet, hoy.minusDays(1));
        prepararMascotaVetMedicamento(mascota, vet);
        when(citas.findById(51L)).thenReturn(Optional.of(cita));
        when(formulas.save(any())).thenAnswer(call -> call.getArgument(0));

        FormulaResponse response = service.crear(request(4, null, 11L, hoy, 51L));

        assertEquals(hoy, response.fechaEmision());
        assertEquals("Cada 8 horas", response.frecuencia());
        assertEquals(5, response.duracionDias());
        assertEquals("Con alimento", response.indicaciones());
        verify(formulas).save(argThat(f -> !f.getIndicaciones().contains("Fecha de emisión")
                && f.getFechaActualizacion() == null));
    }

    @Test
    void creaFormulaAsociadaSoloACita() {
        LocalDate emision = LocalDate.of(2026, 10, 16);
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cita = cita(51L, mascota, vet, emision.minusDays(1));
        prepararMascotaVetMedicamento(mascota, vet);
        when(citas.findById(51L)).thenReturn(Optional.of(cita));
        when(formulas.save(any())).thenAnswer(call -> call.getArgument(0));

        FormulaResponse response = service.crear(request(4, null, 11L, emision, 51L));

        assertEquals(emision, response.fechaEmision());
        assertNull(response.historiaClinicaId());
        verify(formulas).save(argThat(f -> f.getCita() == cita));
    }

    @Test
    void rechazaFechaAnteriorATantoCitaComoHistoria() {
        LocalDate emision = LocalDate.of(2026, 10, 14);
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cita = cita(51L, mascota, vet, emision.plusDays(1));
        prepararMascotaVet(mascota, vet);
        when(citas.findById(51L)).thenReturn(Optional.of(cita));

        assertThrows(BadRequestException.class,
                () -> service.crear(request(4, null, 11L, emision, 51L)));
        verify(formulas, never()).save(any());
    }

    @Test
    void actualizarTambienRechazaFechaAnteriorALaCita() {
        LocalDate emision = LocalDate.of(2026, 10, 14);
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cita = cita(51L, mascota, vet, emision.plusDays(1));
        FormulaMedica existente = new FormulaMedica();
        when(formulas.findById(71L)).thenReturn(Optional.of(existente));
        prepararMascotaVet(mascota, vet);
        when(citas.findById(51L)).thenReturn(Optional.of(cita));

        assertThrows(BadRequestException.class,
                () -> service.actualizar(71L, request(4, null, 11L, emision, 51L)));
        verify(formulas, never()).save(any());
    }

    @Test
    void rechazaMascotaQueNoCoincideConCita() {
        Mascota mascota = mascota(4, 8L);
        Mascota otraMascota = mascota(5, 8L);
        Veterinario vet = veterinario(11L);
        prepararMascotaVet(mascota, vet);
        when(citas.findById(51L)).thenReturn(Optional.of(cita(51L, otraMascota, vet, LocalDate.now().minusDays(1))));

        assertThrows(BadRequestException.class,
                () -> service.crear(request(4, null, 11L, LocalDate.now(), 51L)));
        verify(formulas, never()).save(any());
    }

    @Test
    void rechazaClienteDeCitaQueNoEsPropietarioDeMascota() {
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cita = cita(51L, mascota, vet, LocalDate.now().minusDays(1));
        Cliente otroCliente = new Cliente(); otroCliente.setClienteId(9L);
        cita.setCliente(otroCliente);
        prepararMascotaVet(mascota, vet);
        when(citas.findById(51L)).thenReturn(Optional.of(cita));

        assertThrows(BadRequestException.class,
                () -> service.crear(request(4, null, 11L, LocalDate.now(), 51L)));
        verify(formulas, never()).save(any());
    }

    @Test
    void sinIdsDeAsociacionConservaLaResolucionDeCitaDelContratoExistente() {
        LocalDate emision = LocalDate.of(2026, 10, 16);
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cita = cita(51L, mascota, vet, emision.minusDays(1));
        prepararMascotaVetMedicamento(mascota, vet);
        when(citas.findFirstByMascota_MascotaIdAndVeterinario_VeterinarioIdAndEstadoNotOrderByFechaHoraDesc(4, 11L, EstadoCita.CANCELADA))
                .thenReturn(Optional.of(cita));
        when(formulas.save(any())).thenAnswer(call -> call.getArgument(0));

        FormulaResponse response = service.crear(request(4, null, 11L, emision, null));

        verify(formulas).save(argThat(f -> f.getCita() == cita));
    }

    @Test
    void rechazaAusenciaDeAsociacionesCuandoNoPuedeResolverLaCitaLegada() {
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        prepararMascotaVet(mascota, vet);
        when(citas.findFirstByMascota_MascotaIdAndVeterinario_VeterinarioIdAndEstadoNotOrderByFechaHoraDesc(4, 11L, EstadoCita.CANCELADA))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> service.crear(request(4, null, 11L, LocalDate.now(), null)));
        verify(formulas, never()).save(any());
    }

    @Test
    void rechazaAsociarUnaFormulaNuevaAUnaCitaCancelada() {
        LocalDate emision = LocalDate.of(2026, 10, 16);
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cancelada = cita(51L, mascota, vet, emision.minusDays(1));
        cancelada.setEstado(EstadoCita.CANCELADA);
        prepararMascotaVet(mascota, vet);
        when(citas.findById(51L)).thenReturn(Optional.of(cancelada));

        BadRequestException error = assertThrows(BadRequestException.class,
                () -> service.crear(request(4, null, 11L, emision, 51L)));
        assertEquals(409, error.getStatus().value());
        verify(formulas, never()).save(any());
    }

    @Test
    void editarUnaFormulaYaAsociadaAUnaCitaLuegoCanceladaConservaSuHistorico() {
        LocalDate emision = LocalDate.of(2026, 10, 16);
        Mascota mascota = mascota(4, 8L);
        Veterinario vet = veterinario(11L);
        Cita cancelada = cita(51L, mascota, vet, emision.minusDays(1));
        cancelada.setEstado(EstadoCita.CANCELADA);
        FormulaMedica existente = new FormulaMedica();
        existente.setCita(cancelada);
        existente.setFechaCreacion(emision.atStartOfDay());
        when(formulas.findById(71L)).thenReturn(Optional.of(existente));
        prepararMascotaVetMedicamento(mascota, vet);
        when(formulas.save(any())).thenAnswer(call -> call.getArgument(0));

        FormulaResponse response = service.actualizar(71L, request(4, null, 11L, emision, null));

        assertEquals(51L, existente.getCita().getCitaId());
        assertEquals("Cada 8 horas", response.frecuencia());
    }

    @Test
    void rechazaSolicitudSinNingunaAsociacionNiMascotaParaResolverCita() {
        FormulaRequest request = new FormulaRequest(null, null, 11L, LocalDate.now(), "Antibiotico",
                BigDecimal.ONE, "Cada 8 horas", 5, "Con alimento", null, null);

        assertThrows(BadRequestException.class, () -> service.crear(request));
        verifyNoInteractions(formulas);
        verifyNoInteractions(citas);
    }

    @Test
    void aceptaAliasAngularMascotaIdYConservaFiltroPorCliente() {
        when(formulas.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(call -> Page.empty(call.getArgument(1)));

        PageResponse<?> result = service.listar(0, 20, "mascotaId", "DESC",
                8L, null, null, null, null);

        assertNotNull(result);
        ArgumentCaptor<Specification<FormulaMedica>> specification = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(formulas).findAll(specification.capture(), pageable.capture());
        assertNotNull(specification.getValue());
        assertEquals("cita.mascota.mascotaId", pageable.getValue().getSort().getOrderFor("cita.mascota.mascotaId").getProperty());
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getValue().getSort().getOrderFor("cita.mascota.mascotaId").getDirection());
    }

    @Test
    void usaFechaDeEmisionDescendentePorDefecto() {
        when(formulas.findAll(any(Specification.class), any(Pageable.class)))
                .thenAnswer(call -> Page.empty(call.getArgument(1)));

        service.listar(0, 20, null, "DESC", null, null, null, null, null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(formulas).findAll(any(Specification.class), pageable.capture());
        assertEquals("fechaCreacion", pageable.getValue().getSort().iterator().next().getProperty());
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getValue().getSort().iterator().next().getDirection());
    }

    @Test
    void sigueRechazandoCamposDeOrdenamientoNoPermitidos() {
        assertThrows(BadRequestException.class,
                () -> service.listar(0, 20, "propiedadArbitraria", "DESC", null, null, null, null, null));
        verifyNoInteractions(formulas);
    }

    @Test
    void convierteEnNumeroSoloElTextoCompletoDeDosisNumerica() {
        FormulaMedica formula = formulaParaDetalle("12.5", "Frecuencia: Cada 8 horas");
        when(formulas.findById(90L)).thenReturn(Optional.of(formula));

        FormulaResponse response = service.consultar(90L);

        assertEquals(new BigDecimal("12.5"), response.dosis());
        assertEquals("12.5", response.dosisTexto());
    }

    @Test
    void conservaTextoAntiguoConIntervaloSinConvertirloEnDosisDoce() {
        FormulaMedica formula = formulaParaDetalle("Una pastilla cada 12 horas.", "Cambiando Cualquier cosa");
        when(formulas.findById(90L)).thenReturn(Optional.of(formula));

        FormulaResponse response = service.consultar(90L);

        assertNull(response.dosis());
        assertEquals("Una pastilla cada 12 horas.", response.dosisTexto());
        assertNull(response.frecuencia());
        assertNull(response.duracionDias());
        assertEquals("Cambiando Cualquier cosa", response.indicaciones());
    }

    @Test
    void devuelveFrecuenciaNulaCuandoNoEstaAlmacenada() {
        FormulaMedica formula = formulaParaDetalle("1", "Indicaciones: Con alimento");
        when(formulas.findById(90L)).thenReturn(Optional.of(formula));

        FormulaResponse response = service.consultar(90L);

        assertNull(response.frecuencia());
    }

    @Test
    void devuelveDuracionNulaCuandoNoEstaAlmacenada() {
        FormulaMedica formula = formulaParaDetalle("1", "Indicaciones: Con alimento");
        when(formulas.findById(90L)).thenReturn(Optional.of(formula));

        FormulaResponse response = service.consultar(90L);

        assertNull(response.duracionDias());
    }

    private FormulaMedica formulaParaDetalle(String doseText, String indications) {
        Cliente cliente = new Cliente();
        cliente.setClienteId(8L);
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez López");
        Mascota mascota = new Mascota();
        mascota.setMascotaId(4);
        mascota.setNombreMascota("Luna");
        Veterinario vet = veterinario(11L);
        Cita cita = new Cita();
        cita.setCitaId(51L);
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setVeterinario(vet);

        FormulaMedica formula = new FormulaMedica();
        formula.setFormulaMedicaId(90L);
        formula.setCita(cita);
        formula.setMedicamentoEntity(medicamento(31L, "Antibiótico"));
        formula.setDosisTexto(doseText);
        formula.setIndicaciones(indications);
        formula.setFechaCreacion(LocalDateTime.of(2026, 10, 16, 12, 0));
        return formula;
    }

    private void prepararMascotaVet(Mascota mascota, Veterinario vet) {
        when(veterinarios.findById(vet.getVeterinarioId())).thenReturn(Optional.of(vet));
    }

    private void prepararMascotaVetMedicamento(Mascota mascota, Veterinario vet) {
        prepararMascotaVet(mascota, vet);
        when(medicamentos.findFirstByNombreIgnoreCase("Antibiótico"))
                .thenReturn(Optional.of(medicamento(31L, "Antibiótico")));
    }

    private FormulaRequest request(Integer mascotaId, Long historiaId, Long vetId, LocalDate emision, Long citaId) {
        return new FormulaRequest(mascotaId, historiaId, vetId, emision, "Antibiótico",
                BigDecimal.ONE, "Cada 8 horas", 5, "Con alimento", citaId, null);
    }

    private Mascota mascota(int id, long clienteId) {
        Cliente cliente = new Cliente(); cliente.setClienteId(clienteId);
        Mascota mascota = new Mascota(); mascota.setMascotaId(id); mascota.setNombreMascota("Luna");
        mascota.setCliente(cliente);
        return mascota;
    }

    private Veterinario veterinario(long id) {
        Veterinario veterinario = new Veterinario(); veterinario.setVeterinarioId(id);
        veterinario.setNombres("Ana"); veterinario.setApellidos("López");
        return veterinario;
    }

    private HistoriaClinica historia(long id, Mascota mascota, Veterinario veterinario, LocalDate fecha) {
        HistoriaClinica historia = new HistoriaClinica(); historia.setHistoriaClinicaId(id);
        historia.setMascota(mascota); historia.setVeterinario(veterinario);
        historia.setFechaHora(fecha.atStartOfDay());
        return historia;
    }

    private Cita cita(long id, Mascota mascota, Veterinario veterinario, LocalDate fecha) {
        Cita cita = new Cita(); cita.setCitaId(id); cita.setMascota(mascota);
        cita.setVeterinario(veterinario); cita.setCliente(mascota.getCliente());
        cita.setFechaHora(LocalDateTime.of(fecha, java.time.LocalTime.NOON));
        return cita;
    }

    private Medicamento medicamento(long id, String nombre) {
        Medicamento medicamento = new Medicamento(); medicamento.setId(id); medicamento.setNombre(nombre);
        return medicamento;
    }
}
