package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.entity.Cliente;
import co.edu.uniminuto.veterinaria.entity.Mascota;
import co.edu.uniminuto.veterinaria.entity.Medicamento;
import co.edu.uniminuto.veterinaria.entity.Veterinario;
import co.edu.uniminuto.veterinaria.exception.RecursoNoEncontradoException;
import co.edu.uniminuto.veterinaria.exception.ReglaNegocioException;
import co.edu.uniminuto.veterinaria.repository.ClienteRepository;
import co.edu.uniminuto.veterinaria.repository.MascotaRepository;
import co.edu.uniminuto.veterinaria.repository.MedicamentoRepository;
import co.edu.uniminuto.veterinaria.repository.VeterinarioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Componente reutilizable que comprueba que las entidades referenciadas existan y sean coherentes.
 * Lo usan los servicios de citas, historias, anotaciones y fórmulas (evita repetir código).
 */
@Component
public class ValidadorReferencias {

    /** Repositorio de clientes. */
    private final ClienteRepository clienteRepository;

    /** Repositorio de mascotas. */
    private final MascotaRepository mascotaRepository;

    /** Repositorio de veterinarios. */
    private final VeterinarioRepository veterinarioRepository;

    /** Repositorio de medicamentos. */
    private final MedicamentoRepository medicamentoRepository;

    /**
     * Crea el validador con sus dependencias.
     *
     * @param clienteRepository     repositorio de clientes
     * @param mascotaRepository     repositorio de mascotas
     * @param veterinarioRepository repositorio de veterinarios
     * @param medicamentoRepository repositorio de medicamentos
     */
    public ValidadorReferencias(ClienteRepository clienteRepository, MascotaRepository mascotaRepository,
                                VeterinarioRepository veterinarioRepository,
                                MedicamentoRepository medicamentoRepository) {
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.medicamentoRepository = medicamentoRepository;
    }

    /**
     * Obtiene un cliente que exista y esté activo.
     *
     * @param id identificador del cliente
     * @return el cliente
     * @throws RecursoNoEncontradoException si no existe
     * @throws ReglaNegocioException        si está inactivo
     */
    public Cliente clienteActivo(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El cliente seleccionado no existe."));
        if (!cliente.isActivo()) {
            throw ReglaNegocioException.invalida("El cliente seleccionado está inactivo.");
        }
        return cliente;
    }

    /**
     * Obtiene una mascota que exista.
     *
     * @param id identificador de la mascota
     * @return la mascota
     * @throws RecursoNoEncontradoException si no existe
     */
    public Mascota mascota(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La mascota seleccionada no existe."));
    }

    /**
     * Obtiene una mascota activa y comprueba que pertenezca al cliente indicado.
     *
     * @param mascotaId identificador de la mascota
     * @param cliente   cliente que debe ser su dueño
     * @return la mascota
     * @throws RecursoNoEncontradoException si no existe
     * @throws ReglaNegocioException        si pertenece a otro cliente o está inactiva
     */
    public Mascota mascotaDelCliente(Long mascotaId, Cliente cliente) {
        Mascota mascota = mascota(mascotaId);
        if (!mascota.getCliente().getId().equals(cliente.getId())) {
            throw ReglaNegocioException.invalida("La mascota seleccionada no pertenece al cliente seleccionado.");
        }
        if (!mascota.isActivo()) {
            throw ReglaNegocioException.invalida("La mascota seleccionada está inactiva.");
        }
        return mascota;
    }

    /**
     * Obtiene un veterinario que exista y esté activo.
     *
     * @param id identificador del veterinario
     * @return el veterinario
     * @throws RecursoNoEncontradoException si no existe
     * @throws ReglaNegocioException        si está inactivo
     */
    public Veterinario veterinarioActivo(Long id) {
        Veterinario veterinario = veterinarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El veterinario seleccionado no existe."));
        if (!veterinario.isActivo()) {
            throw ReglaNegocioException.invalida("El veterinario seleccionado no está disponible.");
        }
        return veterinario;
    }

    /**
     * Obtiene un medicamento que exista y no esté vencido.
     *
     * @param id identificador del medicamento
     * @return el medicamento
     * @throws RecursoNoEncontradoException si no existe
     * @throws ReglaNegocioException        si está vencido
     */
    public Medicamento medicamentoVigente(Long id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El medicamento seleccionado no existe."));
        if (medicamento.getFechaVence().isBefore(LocalDate.now())) {
            throw ReglaNegocioException.invalida("El medicamento seleccionado está vencido.");
        }
        return medicamento;
    }
}
