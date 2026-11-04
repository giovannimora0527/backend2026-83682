package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ClienteActualizarRq;
import com.uniminuto.clinica.repository.ClienteRepository;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClienteServiceImplTests {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    private ClienteRepository repository;
    private PlatformTransactionManager transactionManager;
    private ClienteServiceImpl service;
    private Cliente existente;

    @BeforeEach
    void preparar() {
        repository = mock(ClienteRepository.class);
        transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        service = new ClienteServiceImpl(repository, VALIDATOR_FACTORY.getValidator(), transactionManager);
        existente = new Cliente();
        existente.setId(7L);
        existente.setUsuarioId(42);
        existente.setActivo(false);
        existente.setTipoDocumento("TI");
        existente.setNumeroDocumento("anterior");
        existente.setNombres("Anterior");
        existente.setApellidos("Anterior");
        existente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        existente.setGenero("M");
        existente.setTelefono("111");
        existente.setDireccion("Anterior");
    }

    @AfterAll
    static void cerrarValidator() {
        VALIDATOR_FACTORY.close();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void actualizarTodosLosCamposConservandoIdUsuarioYActivo(boolean activo) {
        existente.setActivo(activo);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.findByNumeroDocumento("001234")).thenReturn(Optional.empty());
        when(repository.save(existente)).thenReturn(existente);

        Cliente resultado = service.actualizarCliente(7L, solicitud());

        assertSame(existente, resultado);
        assertEquals(7L, resultado.getId());
        assertEquals(42, resultado.getUsuarioId());
        assertEquals(activo, resultado.getActivo());
        assertEquals("CC", resultado.getTipoDocumento());
        assertEquals("001234", resultado.getNumeroDocumento());
        assertEquals("Laura", resultado.getNombres());
        assertEquals("Morales", resultado.getApellidos());
        assertEquals(LocalDate.of(1995, 6, 15), resultado.getFechaNacimiento());
        assertEquals("F", resultado.getGenero());
        assertEquals("3001234567", resultado.getTelefono());
        assertEquals("Calle 10", resultado.getDireccion());
        verify(repository).save(same(existente));
        verify(transactionManager).commit(any());
        verify(transactionManager).getTransaction(argThat(definition ->
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW));
    }

    @Test
    void inexistenteNoGuardaNiConsultaDocumento() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.actualizarCliente(7L, solicitud()));

        verify(repository).findById(7L);
        verifyNoMoreInteractions(repository);
        verify(transactionManager).rollback(any());
    }

    @Test
    void documentoDeOtroClienteNoGuardaNiModificaEntidad() {
        Cliente otro = new Cliente();
        otro.setId(8L);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.findByNumeroDocumento("001234")).thenReturn(Optional.of(otro));

        assertThrows(ConflictException.class, () -> service.actualizarCliente(7L, solicitud()));

        verify(repository, never()).save(any());
        assertEquals("anterior", existente.getNumeroDocumento());
        assertEquals("Anterior", existente.getNombres());
        assertEquals("Anterior", existente.getDireccion());
        verify(transactionManager).rollback(any());
    }

    @Test
    void permiteDocumentoActualYNormalizaAntesDeConsultarYGuardar() {
        existente.setNumeroDocumento("001234");
        ClienteActualizarRq rq = solicitud();
        rq.setNumeroDocumento(" 001234 ");
        rq.setTipoDocumento(" CC ");
        rq.setNombres(" Laura ");
        rq.setApellidos(" Morales ");
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.findByNumeroDocumento("001234")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Cliente resultado = service.actualizarCliente(7L, rq);

        assertEquals("001234", resultado.getNumeroDocumento());
        assertEquals("CC", resultado.getTipoDocumento());
        assertEquals("Laura", resultado.getNombres());
        assertEquals("Morales", resultado.getApellidos());
        verify(repository).findByNumeroDocumento("001234");
        verify(repository).save(existente);
    }

    @Test
    void opcionalesNulosBorranValoresExistentes() {
        ClienteActualizarRq rq = solicitud();
        rq.setGenero(null);
        rq.setTelefono(null);
        rq.setDireccion(null);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        Cliente resultado = service.actualizarCliente(7L, rq);

        assertNull(resultado.getGenero());
        assertNull(resultado.getTelefono());
        assertNull(resultado.getDireccion());
        assertEquals(7L, resultado.getId());
        assertEquals(42, resultado.getUsuarioId());
        assertFalse(resultado.getActivo());
    }

    @ParameterizedTest
    @MethodSource("entradasInvalidas")
    void rechazaDatosInvalidosAntesDeAccederAlRepositorio(Consumer<ClienteActualizarRq> invalidar) {
        ClienteActualizarRq rq = solicitud();
        invalidar.accept(rq);

        assertThrows(BadRequestException.class, () -> service.actualizarCliente(7L, rq));

        verifyNoInteractions(repository, transactionManager);
    }

    static Stream<Consumer<ClienteActualizarRq>> entradasInvalidas() {
        return Stream.of(
                rq -> rq.setTipoDocumento(null),
                rq -> rq.setTipoDocumento(""),
                rq -> rq.setTipoDocumento(" "),
                rq -> rq.setTipoDocumento("A".repeat(11)),
                rq -> rq.setNumeroDocumento(null),
                rq -> rq.setNumeroDocumento(""),
                rq -> rq.setNumeroDocumento(" "),
                rq -> rq.setNumeroDocumento("1".repeat(21)),
                rq -> rq.setNombres(null),
                rq -> rq.setNombres(""),
                rq -> rq.setNombres(" "),
                rq -> rq.setNombres("A".repeat(101)),
                rq -> rq.setApellidos(null),
                rq -> rq.setApellidos(""),
                rq -> rq.setApellidos(" "),
                rq -> rq.setApellidos("A".repeat(101)),
                rq -> rq.setFechaNacimiento(null),
                rq -> rq.setFechaNacimiento(LocalDate.now().plusDays(1)),
                rq -> rq.setGenero(""),
                rq -> rq.setGenero(" "),
                rq -> rq.setGenero("FF"),
                rq -> rq.setTelefono("1".repeat(21)));
    }

    @Test
    void rechazaIdYBodyInvalidosSinAccesoAlRepositorio() {
        assertThrows(BadRequestException.class, () -> service.actualizarCliente(null, solicitud()));
        assertThrows(BadRequestException.class, () -> service.actualizarCliente(0L, solicitud()));
        assertThrows(BadRequestException.class, () -> service.actualizarCliente(-1L, solicitud()));
        assertThrows(BadRequestException.class, () -> service.actualizarCliente(7L, null));
        verifyNoInteractions(repository, transactionManager);
    }

    @Test
    void devuelveElResultadoDelGuardado() {
        Cliente persistido = new Cliente();
        persistido.setId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(persistido);

        assertSame(persistido, service.actualizarCliente(7L, solicitud()));
    }

    @Test
    void duplicadoConcurrenteEnCommitSeClasificaDespuesDeLaTransaccion() {
        Cliente otro = new Cliente();
        otro.setId(8L);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.findByNumeroDocumento("001234"))
                .thenReturn(Optional.empty(), Optional.of(otro));
        when(repository.save(existente)).thenReturn(existente);
        doThrow(new DataIntegrityViolationException("duplicado")).when(transactionManager).commit(any());

        assertThrows(ConflictException.class, () -> service.actualizarCliente(7L, solicitud()));

        var orden = inOrder(repository, transactionManager);
        orden.verify(repository).findById(7L);
        orden.verify(repository).findByNumeroDocumento("001234");
        orden.verify(repository).save(existente);
        orden.verify(transactionManager).commit(any());
        orden.verify(repository).findByNumeroDocumento("001234");
    }

    @Test
    void otroErrorDeIntegridadNoSePresentaComoDocumentoDuplicado() {
        var error = new DataIntegrityViolationException("otra restriccion");
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenThrow(error);

        assertSame(error, assertThrows(DataIntegrityViolationException.class,
                () -> service.actualizarCliente(7L, solicitud())));
        verify(transactionManager).rollback(any());
    }

    @Test
    void duplicadoConcurrenteEnGuardadoSeConsultaDespuesDelRollback() {
        Cliente otro = new Cliente();
        otro.setId(8L);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.findByNumeroDocumento("001234"))
                .thenReturn(Optional.empty(), Optional.of(otro));
        when(repository.save(existente)).thenThrow(new DataIntegrityViolationException("duplicado"));

        assertThrows(ConflictException.class, () -> service.actualizarCliente(7L, solicitud()));

        var orden = inOrder(repository, transactionManager);
        orden.verify(repository).findById(7L);
        orden.verify(repository).findByNumeroDocumento("001234");
        orden.verify(repository).save(existente);
        orden.verify(transactionManager).rollback(any());
        orden.verify(repository).findByNumeroDocumento("001234");
    }

    private static ClienteActualizarRq solicitud() {
        ClienteActualizarRq rq = new ClienteActualizarRq();
        rq.setTipoDocumento("CC");
        rq.setNumeroDocumento("001234");
        rq.setNombres("Laura");
        rq.setApellidos("Morales");
        rq.setFechaNacimiento(LocalDate.of(1995, 6, 15));
        rq.setGenero("F");
        rq.setTelefono("3001234567");
        rq.setDireccion("Calle 10");
        return rq;
    }
}
