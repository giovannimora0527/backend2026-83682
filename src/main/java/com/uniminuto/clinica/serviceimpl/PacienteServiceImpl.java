package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.PacienteDTO;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.repository.PacienteRepository;
import com.uniminuto.clinica.service.PacienteService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacion de {@link PacienteService}.
 *
 * <p>La anotacion {@code @Service} le indica a Spring que esta clase es un
 * "componente de logica de negocio" y que debe crear una instancia de ella
 * automaticamente para poder inyectarla (pasarla) donde se necesite, por
 * ejemplo dentro de un Controller.</p>
 */
@Service
public class PacienteServiceImpl implements PacienteService {

    /** Repositorio para acceder a los datos de Paciente en la base de datos. */
    private final PacienteRepository pacienteRepository;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * <p>Spring detecta automaticamente que esta clase necesita un
     * {@link PacienteRepository} y se lo entrega aqui (esto se llama
     * "inyeccion de dependencias por constructor" y es la forma recomendada
     * de hacerlo en Spring).</p>
     *
     * @param pacienteRepository repositorio de pacientes
     */
    public PacienteServiceImpl(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /** {@inheritDoc} */
    @Override
    public PacienteDTO crearPaciente(PacienteDTO pacienteDTO) {
        // 1. Convertimos el DTO recibido en una entidad Paciente.
        Paciente paciente = convertirAEntidad(pacienteDTO);

        // 2. Guardamos la entidad en la base de datos. El metodo save()
        //    devuelve la entidad ya guardada, con su id generado.
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // 3. Convertimos la entidad guardada de nuevo a DTO para devolverla.
        return convertirADTO(pacienteGuardado);
    }

    /** {@inheritDoc} */
    @Override
    public List<PacienteDTO> listarPacientes() {
        return pacienteRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public PacienteDTO obtenerPacientePorId(Long id) {
        Paciente paciente = buscarPacientePorId(id);
        return convertirADTO(paciente);
    }

    /**
     * Busca un paciente por id directamente como entidad (no como DTO).
     * Este metodo es usado internamente por otros servicios (Cita,
     * HistoriaMedica) que necesitan la entidad completa para poder
     * asociarla a otra entidad antes de guardarla.
     *
     * @param id identificador del paciente
     * @return la entidad Paciente encontrada
     * @throws RecursoNoEncontradoException si no existe un paciente con ese id
     */
    public Paciente buscarPacientePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el paciente con id " + id));
    }

    /**
     * Convierte una entidad {@link Paciente} en su correspondiente
     * {@link PacienteDTO}.
     *
     * @param paciente entidad a convertir
     * @return DTO con los datos del paciente
     */
    private PacienteDTO convertirADTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setId(paciente.getId());
        dto.setNombre(paciente.getNombre());
        dto.setEspecie(paciente.getEspecie());
        dto.setRaza(paciente.getRaza());
        dto.setPropietario(paciente.getPropietario());
        return dto;
    }

    /**
     * Convierte un {@link PacienteDTO} en su correspondiente entidad
     * {@link Paciente}, lista para ser guardada en la base de datos.
     *
     * @param dto DTO a convertir
     * @return entidad Paciente con los datos del DTO
     */
    private Paciente convertirAEntidad(PacienteDTO dto) {
        Paciente paciente = new Paciente();
        paciente.setNombre(dto.getNombre());
        paciente.setEspecie(dto.getEspecie());
        paciente.setRaza(dto.getRaza());
        paciente.setPropietario(dto.getPropietario());
        return paciente;
    }
}
